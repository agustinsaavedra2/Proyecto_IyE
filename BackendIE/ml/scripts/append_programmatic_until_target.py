    #!/usr/bin/env python3
"""Añade variantes programáticas a ml/data/gold.jsonl hasta alcanzar TARGET (por defecto 300).
Lee ml/data/gold.jsonl, deduplica por synthetic_text y añade variantes generadas a partir de seeds seleccionados.
"""
import json
import random
import re
import uuid
from pathlib import Path

TARGET = 300
GOLD = Path('ml/data/gold.jsonl')


def contains_work_with_children(text):
    if not text: return False
    t = text.lower()
    kws = ['niño','niños','niña','niñas','trabajo infantil','adole','menor']
    return any(k in t for k in kws)


def programmatic_variants(seed, n):
    variants = []
    fields = seed.get('fields', {}) or {}
    base_text = (seed.get('synthetic_text') or '').strip()
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
    comp_candidates = []
    for k, v in fields.items():
        if isinstance(v, str) and len(v) > 3 and any(c.isalpha() for c in v):
            comp_candidates.append(v)
    comp = comp_candidates[0] if comp_candidates else 'Empresa X'

    for i in range(n):
        v = {}
        if monto is not None:
            mult = random.choice([0.5, 0.8, 1.0, 1.5, 2.0, 2.5, 3.0])
            new_monto = round(max(0.0, monto * mult), 2)
        else:
            new_monto = random.choice([5.0, 10.0, 20.0, 50.0, 100.0, 250.0])
        new_fields = {k: v for k, v in fields.items()}
        possible_keys = [k for k in new_fields.keys() if 'MONTO' in k.upper() or 'monto' in k.lower()]
        if possible_keys:
            new_fields[possible_keys[0]] = f"{new_monto}"
        else:
            new_fields['MONTO'] = f"{new_monto}"
        new_comp = comp
        if random.random() < 0.6:
            suffix = random.choice([' S.A.', ' SPA', ' LTDA', ' EIRL'])
            new_comp = (comp + suffix)[:80]
            possible_comp_keys = [k for k in new_fields.keys() if 'EMPRESA' in k.upper() or 'RAZON' in k.upper()]
            if possible_comp_keys:
                new_fields[possible_comp_keys[0]] = new_comp
            else:
                new_fields['EMPRESA'] = new_comp
        reason = fields.get('HECHOS', '') or fields.get('MATERIA', '') or base_text
        if not reason or reason.strip()=='.':
            reason = 'Incumplimiento de normativa laboral'
        synthetic = f"La empresa {new_comp} fue sancionada por {reason.strip()} con multa de {new_monto} UTM."
        if new_monto >= 100.0 or contains_work_with_children(reason):
            label = 'Alto'
            reason_label = 'Multa alta o NNA'
        elif new_monto >= 10.0:
            label = 'Medio'
            reason_label = 'Multa moderada'
        else:
            label = 'Bajo'
            reason_label = 'Multa baja o documentación'
        v['id'] = f"append_prog_{uuid.uuid4().hex[:8]}"
        v['fields'] = new_fields
        v['synthetic_text'] = synthetic
        v['label'] = label
        v['reason'] = reason_label
        variants.append(v)
    return variants


def normalize_text(t):
    if not t:
        return ''
    s = str(t).strip().lower()
    s = re.sub(r'\s+', ' ', s)
    return s


def main():
    if not GOLD.exists():
        print('gold.jsonl missing; create or run augment first')
        return 1
    lines = [l.strip() for l in GOLD.read_text(encoding='utf8').splitlines() if l.strip()]
    objs = [json.loads(l) for l in lines]
    seen = set()
    for o in objs:
        seen.add(normalize_text(o.get('synthetic_text') or ' '.join(str(v) for v in o.get('fields',{}).values())))
    current = len(objs)
    print('Current unique records:', current)
    if current >= TARGET:
        print('Already >= target')
        return 0
    # choose seeds from existing objs (prefer original seeds first)
    seeds = objs.copy()
    random.shuffle(seeds)
    idx = 0
    appended = 0
    with GOLD.open('a', encoding='utf8') as f:
        while current < TARGET:
            seed = seeds[idx % len(seeds)]
            variants = programmatic_variants(seed, 1)
            for v in variants:
                key = normalize_text(v.get('synthetic_text'))
                if key in seen:
                    continue
                f.write(json.dumps(v, ensure_ascii=False) + '\n')
                seen.add(key)
                current += 1
                appended += 1
                if current >= TARGET:
                    break
            idx += 1
    print('Appended', appended, 'programmatic records; final count', current)
    return 0

if __name__=='__main__':
    raise SystemExit(main())

