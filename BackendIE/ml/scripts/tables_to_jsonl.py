#!/usr/bin/env python3
"""Convierte CSVs extraidos en registros JSONL estructurados por fila.
Genera ml/data/jsonl/{pdf_stem}_tableXX_pageYYY.jsonl con una línea por fila.
"""
import argparse
from pathlib import Path
import pandas as pd
import json
from datetime import datetime


def row_to_record(pdf_stem, table_id, row_number, row_series):
    # Convierte una fila pandas Series en un registro estructurado
    fields = {str(k): ("" if pd.isna(v) else str(v)) for k, v in row_series.items()}
    empresa = fields.get('EMPRESA RAZÓN SOCIAL', fields.get('EMPRESA RAZON SOCIAL', fields.get('empresa', '')))
    rut = fields.get('EMPRESA RUT', fields.get('RUT', fields.get('rut', '')))
    fecha = fields.get('FECHA EJECUTORIA', fields.get('FECHA', fields.get('fecha', '')))
    monto = fields.get('MONTO MULTA', fields.get('MONTO', fields.get('monto', '')))
    hechos = fields.get('HECHOS CONDENADOS', fields.get('HECHOS', fields.get('hechos', '')))
    motivo = hechos or fields.get('TIPO DE DENUNCIA', fields.get('TIPO', ''))
    # synthetic text - concise
    synthetic = []
    if empresa:
        synthetic.append(f"La empresa {empresa}")
    if rut:
        synthetic.append(f"(RUT {rut})")
    if fecha:
        synthetic.append(f"fue sancionada el {fecha}")
    if motivo:
        synthetic.append(f"por {motivo}")
    if monto:
        synthetic.append(f"con multa de {monto} UTM")
    synthetic_text = ". ".join([s for s in synthetic if s]) + "."
    record = {
        'id': f"{pdf_stem}_table{table_id}_row{row_number}",
        'source_pdf': pdf_stem,
        'table_id': f"table_{table_id}",
        'row_number': int(row_number),
        'fields': fields,
        'synthetic_text': synthetic_text,
        'metadata': {
            'extracted_at': datetime.utcnow().isoformat() + 'Z'
        }
    }
    return record


def process_csv_file(csv_path, out_dir):
    csv_path = Path(csv_path)
    out_dir = Path(out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    # infer pdf stem and table id from filename pattern
    stem = csv_path.stem
    # expected pattern: {pdf_stem}_tableXX_pageYYY
    parts = stem.split('_table')
    if len(parts) >= 2:
        pdf_stem = parts[0]
        rest = parts[1]
        table_id = ''.join(ch for ch in rest if ch.isdigit())[:2] or '0'
    else:
        pdf_stem = stem
        table_id = '0'
    df = pd.read_csv(csv_path, dtype=str).fillna("")
    out_file = out_dir / f"{pdf_stem}_table{int(table_id):02d}.jsonl"
    with open(out_file, 'w', encoding='utf8') as f:
        for i, row in enumerate(df.itertuples(index=False), start=1):
            # pandas namedtuple has fields at indices; safer to use df.iloc
            series = df.iloc[i-1]
            rec = row_to_record(pdf_stem, int(table_id), i, series)
            f.write(json.dumps(rec, ensure_ascii=False) + '\n')
    return out_file


def process_dir(csv_dir, out_dir):
    p = Path(csv_dir)
    csvs = list(p.glob('*.csv'))
    results = []
    for c in csvs:
        print(f"Processing {c.name}")
        o = process_csv_file(c, out_dir)
        results.append(str(o))
    print(f"Generated {len(results)} jsonl files")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Convert CSV tables to JSONL records')
    parser.add_argument('--csv-dir', type=str, default='ml/data/csv', help='Directorio con CSVs')
    parser.add_argument('--out', type=str, default='ml/data/jsonl', help='Directorio de salida JSONL')
    args = parser.parse_args()
    process_dir(args.csv_dir, args.out)

