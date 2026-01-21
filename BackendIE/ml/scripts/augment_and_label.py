#!/usr/bin/env python3
"""Aumenta el dataset gold usando Ollama (CLI preferido) y/o generación programática.
Salida: ml/data/gold_augmented.jsonl (incluye originales + nuevos)

Uso:
  python ml/scripts/augment_and_label.py --per-seed 5 --out ml/data/gold_augmented.jsonl

Requisitos: Ollama CLI `ollama` (preferido). Si Ollama falla, se usa generación programática para completar hasta 300 casos.
"""
import argparse
import json
import time
from pathlib import Path
import subprocess, shlex
import random
import re
import uuid

OLLAMA_CLI = 'ollama'
MODEL = 'llama3.1:8b'
PROMPT_GEN = (
    "Eres un experto legal chileno especializado en cumplimiento laboral. "
    "Recibirás un caso de sanción en formato JSON con campos 'fields' y 'synthetic_text'. "
    "Genera {n} variantes realistas y distintas del caso original. "
    "Para cada variante devuelve un objeto JSON con claves: id (string), fields (mapa columna->valor), synthetic_text (1-2 oraciones en español), label (Alto|Medio|Bajo), reason (texto corto), multa_monto (número o null). "
    "Responde SOLO con JSON (lista de objetos).\n\nCaso original:\n{case}\n\nList of variants:"
)


def call_ollama_cli(prompt, timeout=120):
    """Run Ollama CLI synchronously and return raw text output or None on failure."""
    cmd = f"{OLLAMA_CLI} run {MODEL} --prompt {shlex.quote(prompt)} --quiet"
    try:
        res = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=timeout)
        out = res.stdout.strip()
        if not out:
            out = res.stderr.strip()
        return out
    except Exception as e:
        # CLI not available or failure
        return None


def parse_json_from_text(text):
    if not text:
        return None
    s = text.strip()
    # try to find an array first
    start = s.find('[')
    end = s.rfind(']')
    if start != -1 and end != -1 and end > start:
        try:
            return json.loads(s[start:end+1])
        except Exception:
            pass
    # fallback: try to load whole text
    try:
        return json.loads(s)
    except Exception:
        pass
    # try to extract individual objects
    objs = []
    for m in re.finditer(r'(\{.*?\})', s, flags=re.S):
        try:
            objs.append(json.loads(m.group(1)))
        except Exception:
            continue
    return objs if objs else None


def normalize_variant(raw_obj, seed_id, idx):
    """Asegura que la variante sea un dict con campos esperados y valores correctos."""
    obj = None
    # if raw_obj is a string that looks like json, try to parse
    if isinstance(raw_obj, str):
        raw = raw_obj.strip()
        try:
            obj = json.loads(raw)
        except Exception:
            # try to find first JSON object inside
            m = re.search(r'(\{.*\})', raw, flags=re.S)
            if m:
                try:
                    obj = json.loads(m.group(1))
                except Exception:
                    obj = None
            else:
                obj = None
    elif isinstance(raw_obj, dict):
        obj = raw_obj
    else:
        # unsupported type (int, float, list of primitives) => skip
        return None

    if obj is None:
        return None

    # ensure fields
    if 'fields' not in obj or not isinstance(obj.get('fields'), dict):
        obj['fields'] = obj.get('fields') or {}
        if isinstance(obj['fields'], str):
            # try to parse inner JSON-like content separated by ':' tokens
            try:
                # naive heuristic: look for key:value pairs
                pairs = [p.strip() for p in obj['fields'].split('||') if p.strip()]
                d = {}
                for p in pairs:
                    if ':' in p:
                        k,v = p.split(':',1)
                        d[k.strip()] = v.strip()
                if d:
                    obj['fields'] = d
                else:
                    obj['fields'] = {}
            except Exception:
                obj['fields'] = {}

    # synthetic_text must be string
    synth = obj.get('synthetic_text')
    if synth is None:
        # try build from fields
        synth = ' '.join(str(v) for v in obj.get('fields', {}).values() if v)
    obj['synthetic_text'] = str(synth).strip()

    # normalize label
    lab = obj.get('label')
    if isinstance(lab, str):
        l = lab.strip().lower()
        if l.startswith('a'):
            obj['label'] = 'Alto'
        elif l.startswith('m'):
            obj['label'] = 'Medio'
        elif l.startswith('b'):
            obj['label'] = 'Bajo'
        else:
            obj['label'] = 'Medio'
    else:
        obj['label'] = 'Medio'

    # ensure id
    if not obj.get('id'):
        obj['id'] = f"aug_{seed_id}_{idx}"

    return obj


def programmatic_variants(seed, n):
    """Genera variantes sintéticas simples a partir de un seed sin depender de Ollama.
    Cambia montos, nombres y fechas mínimamente para mantener coherencia.
    """
    variants = []
    fields = seed.get('fields', {}) or {}
    base_text = (seed.get('synthetic_text') or '').strip()
    # heurística: intentar extraer monto
    monto = None
    for v in fields.values():
        if isinstance(v, str):
            m = re.search(r"(\d+[\.,]?\d*)", v)
            if m:
                try:
                    monto = float(m.group(1).replace(',', '.'))
                    break
                except Exception:
                    continue
    # simple company name alteration
    comp_candidates = []
    for k, v in fields.items():
        if isinstance(v, str) and len(v) > 3 and any(c.isalpha() for c in v):
            comp_candidates.append(v)
    comp = comp_candidates[0] if comp_candidates else 'Empresa X'

    for i in range(n):
        v = {}
        # mutate monto
        new_monto = None
        if monto is not None:
            # random multiplier between 0.5 and 3.0
            mult = random.choice([0.5, 0.8, 1.0, 1.5, 2.0, 2.5, 3.0])
            new_monto = round(max(0.0, monto * mult), 2)
        else:
            # random plausible monto
            new_monto = random.choice([5.0, 10.0, 20.0, 50.0, 100.0, 250.0])
        # build fields: copy original but replace montant-like keys
        new_fields = {k: v for k, v in fields.items()}
        # try to set a key named 'MONTO' or similar
        possible_keys = [k for k in new_fields.keys() if 'MONTO' in k.upper() or 'monto' in k.lower()]
        if possible_keys:
            new_fields[possible_keys[0]] = f"{new_monto}"
        else:
            new_fields['MONTO'] = f"{new_monto}"
        # mutate company name
        new_comp = comp
        if random.random() < 0.6:
            suffix = random.choice([' S.A.', ' SPA', ' LTDA', ' EIRL'])
            new_comp = (comp + suffix)[:80]
            # assign to a plausible key
            possible_comp_keys = [k for k in new_fields.keys() if 'EMPRESA' in k.upper() or 'RAZON' in k.upper()]
            if possible_comp_keys:
                new_fields[possible_comp_keys[0]] = new_comp
            else:
                new_fields['EMPRESA'] = new_comp
        # synthetic text: concise sentence
        reason = fields.get('HECHOS', '') or fields.get('MATERIA', '') or base_text
        if not reason or reason.strip()=='.':
            reason = 'Incumplimiento de normativa laboral'
        synthetic = f"La empresa {new_comp} fue sancionada por {reason.strip()} con multa de {new_monto} UTM."
        # label heuristics
        if new_monto >= 100.0 or contains_work_with_children(reason):
            label = 'Alto'
            reason_label = 'Multa alta o NNA'
        elif new_monto >= 10.0:
            label = 'Medio'
            reason_label = 'Multa moderada'
        else:
            label = 'Bajo'
            reason_label = 'Multa baja o documentación'
        v['id'] = f"aug_prog_{uuid.uuid4().hex[:8]}"
        v['fields'] = new_fields
        v['synthetic_text'] = synthetic
        v['label'] = label
        v['reason'] = reason_label
        variants.append(v)
    return variants

# small util to detect NNA keywords
def contains_work_with_children(text):
    if not text: return False
    t = text.lower()
    kws = ['niño','niños','niña','niñas','trabajo infantil','adole','menor']
    return any(k in t for k in kws)

# main logic: prefer CLI, fallback to programmatic variants when Ollama fails or returns unparsable output
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--per-seed', type=int, default=5)
    parser.add_argument('--out', default='ml/data/gold_augmented.jsonl')
    parser.add_argument('--seed', type=int, default=42)
    parser.add_argument('--sleep', type=float, default=0.25)
    args = parser.parse_args()

    random.seed(args.seed)
    seed_path = Path('ml/data/gold.jsonl')
    out_path = Path(args.out)
    out_path.parent.mkdir(parents=True, exist_ok=True)

    if not seed_path.exists():
        print('Seed gold not found:', seed_path)
        return

    seeds = [json.loads(l) for l in seed_path.read_text(encoding='utf8').splitlines() if l.strip()]
    print(f'Loaded {len(seeds)} seed records')

    existing_texts = set()
    if out_path.exists():
        print('Appending to existing', out_path)
    with out_path.open('a', encoding='utf8') as outf:
        for s in seeds:
            outf.write(json.dumps(s, ensure_ascii=False) + '\n')
            existing_texts.add((s.get('synthetic_text') or '').strip())

    total_needed = max(0, 300 - len(seeds))
    print('Total needed to reach 300:', total_needed)
    if total_needed <= 0:
        print('Already >=300; exiting')
        return

    produced = 0
    idx = 0
    # iterate seeds and try to produce variants until reach target
    for seed in seeds:
        if produced >= total_needed:
            break
        print('Processing seed', seed.get('id'))
        # try CLI first
        prompt = PROMPT_GEN.format(n=args.per_seed, case=json.dumps(seed, ensure_ascii=False))
        cli_out = call_ollama_cli(prompt, timeout=120)
        parsed = None
        if cli_out:
            parsed = parse_json_from_text(cli_out)
        if not parsed:
            # fall back to programmatic generation (guaranteed)
            needed = min(args.per_seed, total_needed - produced)
            print(f'Ollama CLI failed or returned unparsable data for seed {seed.get("id")}, generating {needed} programmatic variants')
            variants = programmatic_variants(seed, needed)
        else:
            # normalize parsed list
            if isinstance(parsed, dict):
                parsed_list = [parsed]
            elif isinstance(parsed, list):
                parsed_list = parsed
            else:
                parsed_list = []
            variants = []
            for raw in parsed_list:
                try:
                    if isinstance(raw, (str, dict)):
                        # ensure dict
                        obj = raw if isinstance(raw, dict) else json.loads(raw)
                        # basic normalization
                        if 'synthetic_text' not in obj or not obj.get('synthetic_text'):
                            obj['synthetic_text'] = ' '.join(str(v) for v in obj.get('fields', {}).values())
                        if 'label' not in obj:
                            obj['label'] = 'Medio'
                        obj['id'] = obj.get('id') or f"aug_cli_{uuid.uuid4().hex[:8]}"
                        variants.append(obj)
                except Exception:
                    continue
        # write variants ensuring uniqueness
        count_added = 0
        with out_path.open('a', encoding='utf8') as outf:
            for v in variants:
                txt = (v.get('synthetic_text') or '').strip()
                if not txt:
                    continue
                if txt in existing_texts:
                    continue
                outf.write(json.dumps(v, ensure_ascii=False) + '\n')
                existing_texts.add(txt)
                produced += 1
                count_added += 1
                idx += 1
                if produced >= total_needed:
                    break
        print(f'Added {count_added} variants for seed {seed.get("id")} (total produced={produced})')
        time.sleep(args.sleep)

    print('Finished. Produced', produced, 'new records. Output at', out_path)

if __name__=='__main__':
    main()

