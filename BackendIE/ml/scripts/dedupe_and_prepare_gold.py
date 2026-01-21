#!/usr/bin/env python3
"""Dedupe ml/data/gold_augmented.jsonl, produce ml/data/gold_final.jsonl con N records (default 300).
Prioriza mantener los primeros records (seeds) y elimina duplicados por 'synthetic_text' (normalized).
Hace backup del gold original y reemplaza ml/data/gold.jsonl con el final.
"""
import json
from pathlib import Path
from datetime import datetime
import shutil

TARGET = 300
AUG = Path('ml/data/gold_augmented.jsonl')
OUT = Path('ml/data/gold_final.jsonl')
GOLD = Path('ml/data/gold.jsonl')
BACKUP_DIR = Path('backups')
BACKUP_DIR.mkdir(exist_ok=True)


def normalize_text(t):
    if not t:
        return ''
    s = str(t).strip().lower()
    # collapse whitespace
    import re
    s = re.sub(r'\s+', ' ', s)
    return s


def main():
    if not AUG.exists():
        print('No augmented file found at', AUG)
        return 1
    seen = set()
    kept = []
    # read lines in order and keep unique by synthetic_text normalized
    with AUG.open('r', encoding='utf8') as f:
        for line in f:
            line=line.strip()
            if not line:
                continue
            try:
                obj = json.loads(line)
            except Exception:
                continue
            key = normalize_text(obj.get('synthetic_text') or ' '.join(str(v) for v in obj.get('fields', {}).values()))
            if key in seen:
                continue
            seen.add(key)
            kept.append(obj)
            if len(kept) >= TARGET:
                break
    print(f'Selected {len(kept)} unique records (target {TARGET})')
    # backup original gold.jsonl if exists
    if GOLD.exists():
        ts = datetime.now().strftime('%Y%m%d_%H%M%S')
        bak = BACKUP_DIR / f'gold_backup_{ts}.jsonl'
        shutil.copy2(GOLD, bak)
        print('Backed up', GOLD, 'to', bak)
    # write final gold file
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with OUT.open('w', encoding='utf8') as f:
        for obj in kept:
            f.write(json.dumps(obj, ensure_ascii=False) + '\n')
    # replace gold.jsonl with OUT
    shutil.copy2(OUT, GOLD)
    print('Wrote final gold to', GOLD)
    return 0

if __name__=='__main__':
    raise SystemExit(main())

