#!/usr/bin/env python3
"""Importa ml/data/gold.jsonl a SQLite (prueba rápida) para tener DB local de entrenamiento.
"""
import sqlite3
import json
from pathlib import Path

DB = Path('ml/db/gold.db')
DB.parent.mkdir(parents=True, exist_ok=True)

schema = '''
CREATE TABLE IF NOT EXISTS gold_cases (
  id TEXT PRIMARY KEY,
  source_pdf TEXT,
  table_id TEXT,
  row_number INTEGER,
  fields TEXT,
  synthetic_text TEXT,
  label TEXT,
  reason TEXT
);
'''


def import_gold(jsonl_path):
    conn = sqlite3.connect(DB)
    cur = conn.cursor()
    cur.executescript(schema)
    inserted = 0
    with open(jsonl_path, 'r', encoding='utf8') as f:
        for line in f:
            obj = json.loads(line)
            cur.execute('''INSERT OR REPLACE INTO gold_cases (id, source_pdf, table_id, row_number, fields, synthetic_text, label, reason)
                           VALUES (?,?,?,?,?,?,?,?)''', (
                obj.get('id'), obj.get('source_pdf'), obj.get('table_id'), obj.get('row_number'), json.dumps(obj.get('fields')), obj.get('synthetic_text'), obj.get('label'), obj.get('reason')
            ))
            inserted += 1
    conn.commit()
    conn.close()
    print('Inserted', inserted, 'rows into', DB)


def main():
    p = Path('ml/data/gold.jsonl')
    if not p.exists():
        print('gold.jsonl not found')
        return
    import_gold(str(p))

if __name__=='__main__':
    main()

