#!/usr/bin/env python3
"""
prepare_dataset.py

Pequeño script para preparar un dataset RAG/ML desde documentos locales (PDF/TXT/HTML)
- Lee archivos en ml/dataset/
- Extrae texto (PDF => PyPDF2)
- Crea chunks (por defecto ~1000 caracteres)
- (Opcional) Calcula embeddings con sentence-transformers (all-MiniLM-L6-v2)
- Guarda resultado en ml/index.jsonl con líneas JSON: {"id":..., "text":..., "meta":{...}, "embedding":[...]} (embedding opcional)
- Si existe ml/urls.txt, intentará descargar URLs (PDFs) a ml/dataset/

Uso:
  python ml/prepare_dataset.py --dataset-dir ml/dataset --out ml/index.jsonl --chunk-size 1000 --embed

Notas:
- Recomiendo crear manualmente la carpeta `ml/dataset/` y colocar PDFs/TXT/HTML de "Documentos públicos de normativas chilenas" y "casos de sanciones Dirección del Trabajo".
- Si prefieres, pones una lista de URLs en `ml/urls.txt` (uno por línea). El script intentará descargar solo si la URL apunta a un PDF.
- Para embeddings necesitas instalar sentence-transformers (requirements.txt incluído). Si no quieres embeddings, omite --embed.

"""

import argparse
import json
import os
import re
import sys
from pathlib import Path
from typing import List

try:
    import requests
except Exception:
    requests = None

try:
    from PyPDF2 import PdfReader
except Exception:
    PdfReader = None

try:
    from sentence_transformers import SentenceTransformer
except Exception:
    SentenceTransformer = None


def download_urls(urls_file: Path, dest_dir: Path):
    if requests is None:
        print("requests no está instalado. Omite descarga. Ejecuta: pip install -r ml/requirements.txt")
        return
    with urls_file.open("r", encoding="utf-8") as f:
        for line in f:
            url = line.strip()
            if not url:
                continue
            # Only handle pdf links by default
            if not url.lower().endswith('.pdf'):
                print(f"Saltando (no PDF): {url}")
                continue
            fname = url.split('/')[-1].split('?')[0]
            dest = dest_dir / fname
            if dest.exists():
                print(f"Ya existe {fname}, saltando download")
                continue
            print(f"Descargando {url} -> {dest}")
            try:
                r = requests.get(url, timeout=60)
                r.raise_for_status()
                dest.write_bytes(r.content)
            except Exception as e:
                print(f"Falló descarga {url}: {e}")


def extract_text_from_pdf(path: Path) -> str:
    if PdfReader is None:
        raise RuntimeError("PyPDF2 no está instalado. Ejecuta: pip install -r ml/requirements.txt")
    try:
        reader = PdfReader(str(path))
        texts = []
        for p in reader.pages:
            try:
                texts.append(p.extract_text() or "")
            except Exception:
                texts.append("")
        return "\n".join(texts)
    except Exception as e:
        print(f"Error leyendo PDF {path}: {e}")
        return ""


def extract_text_from_txt(path: Path) -> str:
    try:
        return path.read_text(encoding='utf-8')
    except Exception:
        try:
            return path.read_text(encoding='latin-1')
        except Exception as e:
            print(f"Error leyendo {path}: {e}")
            return ""


def chunk_text(text: str, chunk_size: int = 1000, overlap: int = 200) -> List[str]:
    # Simple chunker by characters with overlap
    if not text:
        return []
    text = re.sub(r"\s+", " ", text).strip()
    chunks = []
    i = 0
    n = len(text)
    while i < n:
        end = min(i + chunk_size, n)
        chunk = text[i:end]
        chunks.append(chunk)
        i = end - overlap if end < n else end
    return chunks


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--dataset-dir', default='ml/dataset')
    parser.add_argument('--out', default='ml/index.jsonl')
    parser.add_argument('--chunk-size', type=int, default=1000)
    parser.add_argument('--overlap', type=int, default=200)
    parser.add_argument('--embed', action='store_true', help='Calcular embeddings con sentence-transformers')
    parser.add_argument('--download-urls', action='store_true', help='Descargar URLs desde ml/urls.txt si existe')
    args = parser.parse_args()

    dataset_dir = Path(args.dataset_dir)
    out_path = Path(args.out)
    dataset_dir.mkdir(parents=True, exist_ok=True)
    out_path.parent.mkdir(parents=True, exist_ok=True)

    urls_file = Path('ml/urls.txt')
    if args.download_urls and urls_file.exists():
        download_urls(urls_file, dataset_dir)

    # load model for embeddings
    embed_model = None
    if args.embed:
        if SentenceTransformer is None:
            print('sentence-transformers no está instalado. Ejecuta: pip install -r ml/requirements.txt')
            print('Continuando sin embeddings...')
        else:
            print('Cargando modelo de embeddings (all-MiniLM-L6-v2)')
            embed_model = SentenceTransformer('all-MiniLM-L6-v2')

    # iterate files
    id_counter = 0
    with out_path.open('w', encoding='utf-8') as fout:
        for p in sorted(dataset_dir.iterdir()):
            if p.is_dir():
                continue
            suffix = p.suffix.lower()
            text = ''
            if suffix == '.pdf':
                print(f'Extrayendo PDF: {p.name}')
                try:
                    text = extract_text_from_pdf(p)
                except Exception as e:
                    print('Error extrayendo PDF:', e)
                    continue
            elif suffix in ['.txt', '.md']:
                text = extract_text_from_txt(p)
            elif suffix in ['.html', '.htm']:
                # minimal HTML strip
                raw = extract_text_from_txt(p)
                text = re.sub('<[^<]+?>', ' ', raw)
            else:
                print(f'Skipping unsupported file type: {p.name}')
                continue

            if not text.strip():
                print(f'No text extracted from {p.name}, skipping')
                continue

            chunks = chunk_text(text, chunk_size=args.chunk_size, overlap=args.overlap)
            for i, chunk in enumerate(chunks):
                record = {
                    'id': f'{p.name}::chunk-{i}',
                    'text': chunk,
                    'meta': {
                        'source': p.name,
                        'empresaId': None
                    }
                }
                if embed_model is not None:
                    emb = embed_model.encode(chunk).tolist()
                    record['embedding'] = emb
                fout.write(json.dumps(record, ensure_ascii=False) + '\n')
                id_counter += 1
    print(f'Finished. Wrote {id_counter} chunks to {out_path}')


if __name__ == '__main__':
    main()

