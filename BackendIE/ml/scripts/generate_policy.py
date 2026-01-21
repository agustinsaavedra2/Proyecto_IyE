#!/usr/bin/env python3
"""Generador de políticas/procedimientos usando Ollama.
Ejemplo: toma un registro (gold o chunk) y genera una política corta adaptada (p.ej. Política de seguridad alimentaria o protocolo de higiene).
Salida: ml/data/policies/{id}_policy.txt
"""
import argparse
import json
from pathlib import Path
import requests

OLLAMA_URL = 'http://localhost:11434/v1/complete'
MODEL = 'llama3.1:8b'

PROMPT_TEMPLATE = (
    "Eres un experto en compliance para MiPymes en Chile. "
    "Dado el siguiente caso o descripción de incumplimiento, genera una política de cumplimiento corta y accionable (máx. 300 palabras). "
    "Incluye: objetivo, alcance, responsabilidades y pasos clave de cumplimiento. Responde en español.\n\nCaso:\n{case}\n\nPolítica:"
)


def call_api(prompt):
    payload = {'model': MODEL, 'prompt': prompt, 'max_tokens': 400}
    r = requests.post(OLLAMA_URL, json=payload, timeout=60)
    r.raise_for_status()
    data = r.json()
    if isinstance(data, dict) and 'text' in data:
        return data['text']
    return r.text


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--id', help='ID of gold record')
    parser.add_argument('--source', default='ml/data/gold.jsonl')
    parser.add_argument('--out-dir', default='ml/data/policies')
    args = parser.parse_args()

    src = Path(args.source)
    outdir = Path(args.out_dir)
    outdir.mkdir(parents=True, exist_ok=True)
    if not src.exists():
        print('Source not found', src)
        return
    # find record
    rec = None
    with src.open('r', encoding='utf8') as f:
        for line in f:
            o = json.loads(line)
            if args.id and o.get('id')==args.id:
                rec = o
                break
            if not args.id and rec is None:
                rec = o
                break
    if rec is None:
        print('Record not found')
        return
    case = json.dumps(rec, ensure_ascii=False)
    prompt = PROMPT_TEMPLATE.format(case=case)
    txt = call_api(prompt)
    out_path = outdir / f"{rec.get('id')}_policy.txt"
    out_path.write_text(txt, encoding='utf8')
    print('Wrote', out_path)

if __name__=='__main__':
    main()

