#!/usr/bin/env python3
"""Extrae tablas de PDFs en ml/dataset usando pdfplumber y guarda CSVs en ml/data/csv.
Uso:
  python ml/scripts/extract_tables.py --pdf "ml/dataset/file.pdf" --out-dir ml/data/csv
Si no se pasa --pdf, procesa todos los PDFs en ml/dataset.
"""
import argparse
import os
import pdfplumber
import pandas as pd
from pathlib import Path


def clean_cell(x):
    if x is None:
        return ""
    return " ".join(str(x).split())


def extract_pdf_tables(pdf_path, out_dir):
    pdf_path = Path(pdf_path)
    out_dir = Path(out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    tables_found = 0
    with pdfplumber.open(str(pdf_path)) as pdf:
        for i, page in enumerate(pdf.pages, start=1):
            try:
                page_tables = page.extract_tables()
            except Exception:
                page_tables = []
            if not page_tables:
                continue
            for t_idx, table in enumerate(page_tables, start=1):
                # table is list of rows (lists)
                if not table:
                    continue
                # Use first row as header if it looks like strings
                header = table[0]
                rows = table[1:]
                # If header has many None or numeric, fallback to generic
                use_header = any(h and str(h).strip() for h in header)
                if use_header:
                    cols = [str(h).strip() if h else f"col_{j}" for j, h in enumerate(header, start=1)]
                else:
                    # fallback header
                    max_cols = max(len(r) for r in table)
                    cols = [f"col_{j}" for j in range(1, max_cols+1)]
                    rows = table
                # Normalize rows to same length
                norm_rows = []
                for r in rows:
                    r = r or []
                    r = [clean_cell(c) for c in r]
                    if len(r) < len(cols):
                        r = r + [""] * (len(cols) - len(r))
                    elif len(r) > len(cols):
                        r = r[:len(cols)]
                    norm_rows.append(r)
                df = pd.DataFrame(norm_rows, columns=cols)
                safe_stem = pdf_path.stem.replace(' ', '_').replace(':','')
                out_name = f"{safe_stem}_table{t_idx:02d}_page{i:03d}.csv"
                out_file = out_dir / out_name
                df.to_csv(out_file, index=False)
                tables_found += 1
    return tables_found


def process_all(dataset_dir, out_dir):
    p = Path(dataset_dir)
    pdfs = list(p.glob("*.pdf"))
    total = 0
    for pdf in pdfs:
        n = extract_pdf_tables(pdf, out_dir)
        print(f"Processed {pdf.name}: {n} table(s)")
        total += n
    print(f"Total tables extracted: {total}")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Extraer tablas de PDFs a CSV')
    parser.add_argument('--pdf', type=str, help='Ruta al PDF a procesar')
    parser.add_argument('--dataset-dir', type=str, default='ml/dataset', help='Directorio con PDFs (si no se usa --pdf)')
    parser.add_argument('--out-dir', type=str, default='ml/data/csv', help='Directorio de salida para CSVs')
    args = parser.parse_args()
    if args.pdf:
        n = extract_pdf_tables(args.pdf, args.out_dir)
        print(f"Extracted {n} tables from {args.pdf}")
    else:
        process_all(args.dataset_dir, args.out_dir)

