#!/usr/bin/env python3
"""
normalize_labels.py

Lee `ml/index_labeled.jsonl`, normaliza el campo `label.riesgo` a valores estandar: 'bajo', 'medio', 'alto'
Si el valor es 'nulo', 'null', 'none' o similar lo convierte a None.
Escribe el resultado en `ml/index_labeled_normalized.jsonl` y muestra un resumen de conteos.

Uso:
  python ml/normalize_labels.py
"""
import json
from pathlib import Path

IN = Path('index_labeled.jsonl')
OUT = Path('index_labeled_normalized.jsonl')

if not IN.exists():
    print('Input file not found:', IN)
    raise SystemExit(1)

NORMAL_MAP = {
    'alto': 'alto', 'high': 'alto', 'a': 'alto',
    'medio': 'medio', 'medium': 'medio', 'm': 'medio',
    'bajo': 'bajo', 'low': 'bajo', 'l': 'bajo',
    'nulo': None, 'null': None, 'none': None, 'na': None, '': None
}

counts = {'alto':0, 'medio':0, 'bajo':0, 'none':0}

with IN.open('r', encoding='utf-8') as fin, OUT.open('w', encoding='utf-8') as fout:
    for line in fin:
        line=line.strip()
        if not line:
            continue
        try:
            obj = json.loads(line)
        except Exception as e:
            print('WARN: skipping invalid json line:', e)
            continue
        lab = obj.get('label')
        if isinstance(lab, dict):
            raw = lab.get('riesgo')
            # sometimes model returns strings like 'Alto', 'alto', or nonstandard tokens
            if raw is None:
                norm = None
            else:
                s = str(raw).strip().lower()
                # try direct mapping
                norm = NORMAL_MAP.get(s)
                # try to clean punctuation
                if norm is None and s:
                    s2 = ''.join(c for c in s if c.isalpha())
                    norm = NORMAL_MAP.get(s2)
            # set normalized label back
            if norm is None:
                obj['label']['riesgo'] = None
                counts['none'] += 1
            else:
                obj['label']['riesgo'] = norm
                counts[norm] += 1
        else:
            # no label object, skip
            counts['none'] += 1
        fout.write(json.dumps(obj, ensure_ascii=False) + '\n')

print('Normalization done. Summary:')
print('\n'.join([f"{k}: {v}" for k,v in counts.items()]))
print('Wrote:', OUT)

