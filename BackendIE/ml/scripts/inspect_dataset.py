#!/usr/bin/env python3
"""Inspector rápido para revisar ml/data/gold.jsonl y ml/chunks
Muestra conteos y primeras N líneas para ver calidad.
"""
import json
from pathlib import Path


def show_head(path, n=5):
    print('==', path, '==')
    p = Path(path)
    if not p.exists():
        print('MISSING')
        return
    with p.open('r', encoding='utf8') as f:
        for i, line in enumerate(f):
            if i>=n: break
            print(line.strip())
    print()


def count_files(dirpath, pattern='*'):
    p = Path(dirpath)
    if not p.exists():
        return 0
    return len(list(p.glob(pattern)))


def main():
    print('Gold count and head:')
    show_head('ml/data/gold.jsonl', 10)
    print('Chunks count:', count_files('ml/chunks','*.json'))
    print('Sample chunk head:')
    sample = next(Path('ml/chunks').glob('*.json'), None)
    if sample:
        show_head(sample, 1)
    print('JSONL files:')
    for f in Path('ml/data/jsonl').glob('*.jsonl'):
        print('-', f.name)

if __name__=='__main__':
    main()

