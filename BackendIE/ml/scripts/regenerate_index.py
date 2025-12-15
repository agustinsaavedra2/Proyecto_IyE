#!/usr/bin/env python3
"""Regenera ml/index.jsonl desde el manifest de chunks.
Cada entrada en index.jsonl contendrá referencia a chunks relevantes y metadatos.
"""
import argparse
from pathlib import Path
import json


def regenerate_index(manifest_path, out_index):
    manifest_path = Path(manifest_path)
    out_index = Path(out_index)
    out_index.parent.mkdir(parents=True, exist_ok=True)
    with open(manifest_path, 'r', encoding='utf8') as f:
        manifest = json.load(f)
    chunks = manifest.get('chunks', [])
    # Index agrupado por source_pdf
    index_entries = {}
    for c in chunks:
        src = c.get('source_pdf') or 'unknown'
        if src not in index_entries:
            index_entries[src] = {
                'source_pdf': src,
                'chunks': []
            }
        index_entries[src]['chunks'].append(c)
    # write index as jsonl: one entry per source_pdf
    with open(out_index, 'w', encoding='utf8') as oi:
        for src, entry in index_entries.items():
            oi.write(json.dumps(entry, ensure_ascii=False) + '\n')
    print(f"Wrote index with {len(index_entries)} entries to {out_index}")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Regenerate index.jsonl from chunks manifest')
    parser.add_argument('--manifest', type=str, default='ml/chunks/manifest.json', help='Manifest path')
    parser.add_argument('--out', type=str, default='ml/index.jsonl', help='Output index.jsonl')
    args = parser.parse_args()
    regenerate_index(args.manifest, args.out)

