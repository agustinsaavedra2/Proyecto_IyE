#!/usr/bin/env python3
"""
normalize_and_score.py

Normaliza las etiquetas generadas en ml/labeled/ y produce un CSV gold_candidates.csv para revisión humana.
"""
import os
import json
import re
import csv
from tqdm import tqdm

LAB_DIR = "ml/labeled"
OUT_CSV = "ml/gold_candidates.csv"


def extract_monto(text):
    # busca patrones UF, UTM, $ y números
    t = text.replace('.', '').replace(',', '')
    m = re.search(r"(\d{1,4}\s*(?:UF|UTM|USD|\$))", text, flags=re.I)
    if m:
        return m.group(1)
    m2 = re.search(r"(\d{1,6})\s*uf", text, flags=re.I)
    if m2:
        return m2.group(1) + " UF"
    return ""

rows = []
for f in tqdm(sorted(os.listdir(LAB_DIR))):
    if not f.endswith('.json'): continue
    path = os.path.join(LAB_DIR, f)
    with open(path, 'r', encoding='utf-8') as fi:
        obj = json.load(fi)
    label = obj.get('label', {})
    riesgo = None
    razones = []
    confianza = None
    if isinstance(label, dict):
        riesgo = label.get('riesgo') or label.get('risk') or None
        razones = label.get('razones') or label.get('motivos') or label.get('reasons') or []
        confianza = label.get('confianza') or label.get('confianza_estimada') or label.get('confianza') or label.get('confidence') or None
    # fallback: try to infer from text if no label
    text = obj.get('text','')
    monto = extract_monto(text)
    # simple fallback heuristics if riesgo missing
    if not riesgo:
        t = text.lower()
        if 'trabajo de menores' in t or 'nna' in t or 'prácticas antisindical' in t or 'antisindical' in t:
            riesgo = 'alto'
        elif 'multa' in t and ('uf' in t or 'utm' in t):
            riesgo = 'medio'
        else:
            riesgo = 'medio'
    if confianza is None:
        # heurística: si razones no vacías -> confianza mayor
        confianza = 0.9 if razones else 0.6

    rows.append({
        'id': obj['id'],
        'source_pdf': obj['source_pdf'],
        'chunk_index': obj['chunk_index'],
        'monto': monto,
        'riesgo': riesgo,
        'razones': '; '.join(razones) if isinstance(razones,(list,tuple)) else str(razones),
        'confianza': float(confianza) if isinstance(confianza,(int,float)) else 0.6,
        'text_snippet': text[:800].replace("\n"," "),
        'model_raw': obj.get('model_raw','')[:400]
    })

# Save CSV for human review
with open(OUT_CSV, 'w', newline='', encoding='utf-8') as cf:
    if len(rows) == 0:
        print('No rows to write')
    else:
        w = csv.DictWriter(cf, fieldnames=list(rows[0].keys()))
        w.writeheader()
        for r in rows:
            w.writerow(r)
print('Wrote', OUT_CSV, 'with', len(rows), 'rows.')

