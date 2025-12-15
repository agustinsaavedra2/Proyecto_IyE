#!/usr/bin/env python3
"""Genera chunks JSONL a partir de los jsonl por tabla. Crea ml/chunks/*.jsonl y un manifest.json.
"""
import argparse
from pathlib import Path
import json
from datetime import datetime


def estimate_tokens(text):
    # heurística rápida: 1 token ~= 4 chars
    return max(1, int(len(text) / 4))


def generate_chunks_from_record(rec, max_chars=3500):
    text = rec.get('synthetic_text', '')
    # si no hay synthetic_text, construir a partir de fields
    if not text.strip():
        fields = rec.get('fields', {})
        text = ' '.join(str(v) for k, v in fields.items() if v)
    if not text:
        return []
    # dividir si es muy largo por oraciones
    if len(text) <= max_chars:
        return [text]
    else:
        parts = text.split('. ')
        chunks = []
        cur = ''
        for p in parts:
            candidate = (cur + '. ' + p).strip() if cur else p
            if len(candidate) > max_chars:
                if cur:
                    chunks.append(cur.strip())
                cur = p
            else:
                cur = candidate
        if cur:
            chunks.append(cur.strip())
        return chunks


def process_jsonl_file(jsonl_path, chunks_dir, max_chars):
    jsonl_path = Path(jsonl_path)
    chunks_dir = Path(chunks_dir)
    chunks_dir.mkdir(parents=True, exist_ok=True)
    manifest_entries = []
    with open(jsonl_path, 'r', encoding='utf8') as f:
        for line in f:
            rec = json.loads(line)
            rec_id = rec.get('id')
            chunks = generate_chunks_from_record(rec, max_chars=max_chars)
            for i, ch in enumerate(chunks, start=1):
                chunk_id = f"{rec_id}_chunk{i:02d}"
                chunk_obj = {
                    'chunk_id': chunk_id,
                    'source_pdf': rec.get('source_pdf'),
                    'text': ch,
                    'records': [rec_id],
                    'tokens': estimate_tokens(ch),
                    'metadata': {
                        'generated_at': datetime.utcnow().isoformat() + 'Z'
                    }
                }
                out_file = chunks_dir / f"{chunk_id}.json"
                with open(out_file, 'w', encoding='utf8') as cf:
                    json.dump(chunk_obj, cf, ensure_ascii=False)
                manifest_entries.append({
                    'chunk_id': chunk_id,
                    'path': str(out_file.relative_to(Path.cwd())),
                    'source_pdf': rec.get('source_pdf'),
                    'records': [rec_id],
                    'tokens': chunk_obj['tokens']
                })
    return manifest_entries


def process_dir(jsonl_dir, chunks_dir, manifest_path, max_chars):
    p = Path(jsonl_dir)
    jsonls = list(p.glob('*.jsonl'))
    all_entries = []
    for j in jsonls:
        print(f"Processing {j.name}")
        ent = process_jsonl_file(j, chunks_dir, max_chars)
        all_entries.extend(ent)
    # write manifest
    manifest = {
        'generated_at': datetime.utcnow().isoformat() + 'Z',
        'chunks': all_entries
    }
    with open(manifest_path, 'w', encoding='utf8') as mf:
        json.dump(manifest, mf, ensure_ascii=False, indent=2)
    print(f"Wrote manifest with {len(all_entries)} chunks to {manifest_path}")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Generar chunks desde jsonl por tabla')
    parser.add_argument('--jsonl-dir', type=str, default='ml/data/jsonl', help='Directorio con jsonl')
    parser.add_argument('--chunks-dir', type=str, default='ml/chunks', help='Directorio salida chunks')
    parser.add_argument('--manifest', type=str, default='ml/chunks/manifest.json', help='Ruta manifest')
    parser.add_argument('--max-chars', type=int, default=3500, help='Máximo de caracteres por chunk')
    args = parser.parse_args()
    process_dir(args.jsonl_dir, args.chunks_dir, args.manifest, args.max_chars)

