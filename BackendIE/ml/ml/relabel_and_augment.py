#!/usr/bin/env python3
"""
relabel_and_augment.py

- Relabels 'none' entries in index_labeled_normalized.jsonl using local Ollama classification.
- Augments minority class 'medio' by paraphrasing samples via Ollama to reach target counts.
- Writes index_augmented.jsonl in the same folder.

Usage:
  python relabel_and_augment.py --input index_labeled_normalized.jsonl --out index_augmented.jsonl --target-per-class 120 --paraphrases-per-medio 20

Be careful: this will call local Ollama many times.
"""
import argparse
import json
import time
from pathlib import Path
import requests
import math

OLLAMA_BASE = 'http://localhost:11434'
MODEL = 'llama3.1:8b'

CLASSIFY_PROMPT = (
    "Eres un experto legal en Chile.\n\nClasifica el siguiente fragmento en RIESGO: 'alto', 'medio' o 'bajo'.\n" 
    "Responde SOLO con la palabra: alto|medio|bajo.\n\nTexto:\n<<<\n{txt}\n>>>\n"
)

PARAPHRASE_PROMPT = (
    "Eres un experto en reescritura de textos legales.\n" 
    "Genera una única paráfrasis breve (manteniendo el significado legal) del siguiente fragmento.\n" 
    "Devuelve SOLO la paráfrasis.\n\nTexto:\n<<<\n{txt}\n>>>\n"
)


def call_ollama_endpoint(payload_builder, timeout=30, max_retries=2):
    """Try several Ollama endpoints with adaptive payloads.
    payload_builder(endpoint) must return JSON payload adapted for that endpoint.
    """
    endpoints = ['/v1/complete', '/v1/completions', '/v1/chat/completions']
    headers = {'Content-Type': 'application/json'}
    last_err = None
    for ep in endpoints:
        url = OLLAMA_BASE.rstrip('/') + ep
        payload = None
        try:
            payload = payload_builder(ep)
        except Exception as e:
            last_err = f'payload build failed: {e}'
            continue

        for attempt in range(max_retries + 1):
            try:
                r = requests.post(url, json=payload, timeout=timeout, headers=headers)
                if r.status_code != 200:
                    last_err = f"HTTP {r.status_code} from {url}: {r.text[:1000]}"
                    # If 400 due to wrong payload shape, try next endpoint
                    if r.status_code == 400:
                        break
                    # otherwise retry
                    time.sleep(0.5 + attempt * 0.5)
                    continue
                data = r.json()
                # support several shapes
                if isinstance(data, dict):
                    if 'text' in data and data['text']:
                        return data['text']
                    if 'result' in data and isinstance(data['result'], dict) and data['result'].get('text'):
                        return data['result']['text']
                    if 'choices' in data and isinstance(data['choices'], list) and len(data['choices']) > 0:
                        c = data['choices'][0]
                        if isinstance(c, dict):
                            if 'message' in c and isinstance(c['message'], dict):
                                return c['message'].get('content', '')
                            if 'text' in c:
                                return c.get('text', '')
                # fallback to raw text
                return r.text
            except requests.exceptions.Timeout as te:
                last_err = f'Timeout: {te}'
                time.sleep(0.5 + attempt * 0.5)
                continue
            except Exception as e:
                last_err = str(e)
                time.sleep(0.5 + attempt * 0.5)
                continue
    raise RuntimeError(f'Ollama call failed. Last error: {last_err}')


def classify_text(text, timeout=20, max_retries=2):
    prompt = CLASSIFY_PROMPT.format(txt=text[:3000])

    def builder(ep):
        # For chat endpoints use messages, for others use prompt
        if ep.endswith('/chat/completions'):
            return {'model': MODEL, 'messages': [{'role': 'user', 'content': prompt}], 'max_tokens': 10}
        else:
            return {'model': MODEL, 'prompt': prompt, 'max_tokens': 10}

    out = call_ollama_endpoint(builder, timeout=timeout, max_retries=max_retries)
    if not out:
        return None
    s = out.strip().lower()
    # pick first token
    for token in ['alto', 'medio', 'bajo']:
        if token in s:
            return token
    return None


def paraphrase_text(text, timeout=30, max_retries=2):
    prompt = PARAPHRASE_PROMPT.format(txt=text[:3000])

    def builder(ep):
        if ep.endswith('/chat/completions'):
            return {'model': MODEL, 'messages': [{'role': 'user', 'content': prompt}], 'max_tokens': 200}
        else:
            return {'model': MODEL, 'prompt': prompt, 'max_tokens': 200}

    out = call_ollama_endpoint(builder, timeout=timeout, max_retries=max_retries)
    if not out:
        return None
    return out.strip()


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--input', default='index_labeled_normalized.jsonl')
    p.add_argument('--out', default='index_augmented.jsonl')
    p.add_argument('--target-per-class', type=int, default=120)
    p.add_argument('--paraphrases-per-medio', type=int, default=20)
    p.add_argument('--sleep', type=float, default=0.2)
    p.add_argument('--max-retries', type=int, default=2)
    p.add_argument('--request-timeout', type=int, default=30)
    args = p.parse_args()

    IN = Path(args.input)
    OUT = Path(args.out)
    if not IN.exists():
        print('Input not found', IN)
        return

    records = []
    with IN.open('r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                obj = json.loads(line)
            except Exception:
                continue
            rid = obj.get('id')
            text = obj.get('text', '')
            label = None
            if isinstance(obj.get('label'), dict):
                label = obj['label'].get('riesgo')
            records.append({'id': rid, 'text': text, 'label': label, 'meta': obj.get('meta', {})})

    # Relabel None entries
    for rec in records:
        if rec['label'] is None:
            try:
                lab = classify_text(rec['text'], timeout=args.request_timeout, max_retries=args.max_retries)
                rec['label'] = lab
                print(f"Relabeled {rec['id']} -> {lab}")
            except Exception as e:
                print('Classify failed for', rec['id'], e)
            time.sleep(args.sleep)

    # Count per class
    from collections import defaultdict
    counts = defaultdict(int)
    for r in records:
        counts[r['label'] if r['label'] else 'none'] += 1
    print('Counts after relabel:', dict(counts))

    # Prepare augmentation lists
    byclass = defaultdict(list)
    for r in records:
        if r['label']:
            byclass[r['label']].append(r)
    for k in ['alto', 'medio', 'bajo']:
        byclass.setdefault(k, [])

    augmented = []
    # Start with original labeled records (exclude ones still None)
    for r in records:
        if r['label']:
            outobj = {'id': r['id'], 'text': r['text'], 'meta': r['meta'], 'label': {'riesgo': r['label']}}
            augmented.append(outobj)

    # Augment 'medio' heavily up to target-per-class
    target = args.target_per_class
    current = len(byclass['medio'])
    need = max(0, target - current)
    print('Need medio:', need)
    if need > 0 and len(byclass['medio']) > 0:
        per_source = max(1, math.ceil(need / len(byclass['medio'])))
        generated = 0
        for src in byclass['medio']:
            if generated >= need:
                break
            # limit paraphrases-per-source to avoid excessive calls
            to_generate = min(per_source, args.paraphrases_per_medio)
            for i in range(to_generate):
                if generated >= need:
                    break
                try:
                    para = paraphrase_text(src['text'], timeout=args.request_timeout, max_retries=args.max_retries)
                except Exception as e:
                    print('Paraphrase failed', e)
                    para = None
                if para:
                    augmented.append({'id': f"{src['id']}_par_{generated}", 'text': para, 'meta': src['meta'], 'label': {'riesgo': 'medio'}})
                    generated += 1
                    if generated % 10 == 0:
                        print('Generated medio paraphrases:', generated)
                else:
                    # backoff a bit on failures
                    time.sleep(args.sleep * 2)
                time.sleep(args.sleep)
        print('Generated total medio paraphrases:', generated)

    # Augment other classes lightly (80% of target)
    for cls in ['alto', 'bajo']:
        current = len(byclass[cls])
        need = max(0, int(target * 0.8) - current)
        if need > 0 and len(byclass[cls]) > 0:
            per_source = max(1, math.ceil(need / len(byclass[cls])))
            gen = 0
            for src in byclass[cls]:
                if gen >= need:
                    break
                for i in range(per_source):
                    if gen >= need:
                        break
                    try:
                        para = paraphrase_text(src['text'], timeout=args.request_timeout, max_retries=args.max_retries)
                    except Exception as e:
                        para = None
                    if para:
                        augmented.append({'id': f"{src['id']}_par_{cls}_{gen}", 'text': para, 'meta': src['meta'], 'label': {'riesgo': cls}})
                        gen += 1
                    time.sleep(args.sleep)
            print(f'Generated {gen} paraphrases for {cls}')

    # Write augmented file
    with OUT.open('w', encoding='utf-8') as fo:
        for o in augmented:
            fo.write(json.dumps(o, ensure_ascii=False) + '\n')
    print('Wrote augmented file:', OUT)
    print('Total augmented samples:', len(augmented))


if __name__ == '__main__':
    main()
