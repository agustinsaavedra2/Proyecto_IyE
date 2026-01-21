#!/usr/bin/env python3
"""Convierte ml/data/gold.jsonl a ml/index.jsonl en el formato que espera trainer.py
Cada línea en ml/index.jsonl tendrá: {"text": ..., "meta": {"source": ...}, "label": ...}
"""
import json
from pathlib import Path


def main():
    gold = Path('ml/data/gold.jsonl')
    out = Path('ml/index.jsonl')
    if not gold.exists():
        print('gold.jsonl no encontrado:', gold)
        return
    out.parent.mkdir(parents=True, exist_ok=True)
    count = 0
    with gold.open('r', encoding='utf8') as f_in, out.open('w', encoding='utf8') as f_out:
        for line in f_in:
            if not line.strip():
                continue
            obj = json.loads(line)
            text = obj.get('synthetic_text') or ' '.join(str(v) for v in obj.get('fields',{}).values())
            if not text or text.strip()=='.':
                # try to build a text from fields
                text = ' '.join(str(v) for v in obj.get('fields',{}).values())
            record = {
                'text': text,
                'meta': {'source': obj.get('source_pdf')},
                'label': obj.get('label')
            }
            f_out.write(json.dumps(record, ensure_ascii=False) + '\n')
            count += 1
    print('Wrote', count, 'records to', out)

if __name__=='__main__':
    main()

