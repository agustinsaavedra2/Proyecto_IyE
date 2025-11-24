#!/usr/bin/env python3
"""
extract_and_chunk.py

Extrae texto de PDFs en `ml/dataset/` y crea chunks relevantes en `ml/chunks/`.
"""
import os
import glob
import re
import json
from pdfminer.high_level import extract_text
from tqdm import tqdm

RAW_DIR = "ml/dataset"          # coloca tus PDFs aquí
OUT_DIR = "ml/chunks"        # result will go here
os.makedirs(OUT_DIR, exist_ok=True)

# Config
CHARS_PER_CHUNK = 2500    # ~500 tokens (ajusta si quieres)
OVERLAP = 500

# Keywords to mark chunk as relevant (sanciones, multas, UF, UTM, sancionado, multa, infracción, resolución)
KEYWORDS = ["multa", "uf", "utm", "sancion", "sancionado", "resolución", "resolucion", "infracción", "infraccion",
            "trabajo de menores", "nna", "antisindical", "higien", "sanitar", "incumplimiento"]


def is_relevant(text):
    t = (text or '').lower()
    return any(k in t for k in KEYWORDS)


def chunk_text(text):
    chunks = []
    start = 0
    L = len(text)
    if L == 0:
        return chunks
    while start < L:
        end = min(start + CHARS_PER_CHUNK, L)
        chunk = text[start:end]
        chunks.append(chunk.strip())
        start = end - OVERLAP
        if start < 0:
            start = 0
    return chunks


idx = 0
manifest = []
for pdf in sorted(glob.glob(os.path.join(RAW_DIR, "*.pdf"))):
    try:
        txt = extract_text(pdf)
    except Exception as e:
        print("Error extracting", pdf, e)
        continue
    chunks = chunk_text(txt)
    for i, c in enumerate(chunks):
        if is_relevant(c):
            fname = f"chunk_{idx:06d}.json"
            obj = {
                "id": fname.replace(".json", ""),
                "source_pdf": os.path.basename(pdf),
                "chunk_index": i,
                "text": c
            }
            with open(os.path.join(OUT_DIR, fname), "w", encoding="utf-8") as fo:
                json.dump(obj, fo, ensure_ascii=False, indent=2)
            manifest.append(obj)
            idx += 1

with open(os.path.join(OUT_DIR, "manifest.json"), "w", encoding="utf-8") as fo:
    json.dump(manifest, fo, ensure_ascii=False, indent=2)

print(f"Created {idx} relevant chunks in {OUT_DIR}.")

