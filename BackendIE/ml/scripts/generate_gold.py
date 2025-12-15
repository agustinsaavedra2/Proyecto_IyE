#!/usr/bin/env python3
"""Genera un archivo ml/data/gold.jsonl con casos 'gold' etiquetados heurísticamente
Usa los JSONL estructurados generados por tables_to_jsonl.py en ml/data/jsonl.
Reglas heurísticas (configurables):
 - Si el campo 'fields' contiene palabras clave sensibles ("niños","trabajo de niños","trabajo infantil") -> Alto
 - Si 'MONTO' o campos similares indican multa >= 100.0 UTM -> Alto
 - Si 10.0 <= monto < 100.0 -> Medio
 - Si monto < 10.0 o monto ausente -> Bajo
 - Si la descripción ('synthetic_text') contiene "sancion" o "mult" y no hay monto -> Medio
Salida:
 - ml/data/gold.jsonl con {id, source_pdf, table_id, row_number, fields, synthetic_text, label, reason}
"""
import json
from pathlib import Path
import re


def parse_monto(fields):
    # Try common keys
    keys = ['MONTO MULTA', 'MONTO', 'monto', 'MONTO MULTA (UTM)', 'MONTO (UTM)']
    val = None
    for k in keys:
        if k in fields and fields[k]:
            val = fields[k]
            break
    if not val:
        # fallback: search any field value that looks like a number with decimals
        for v in fields.values():
            if isinstance(v, str) and re.search(r"\d+[\.,]\d{1,2}", v):
                val = v
                break
    if not val:
        return None
    # Extract number
    m = re.search(r"(\d+[\.,]?\d*)", str(val))
    if not m:
        return None
    num = m.group(1).replace(',', '.')
    try:
        return float(num)
    except Exception:
        return None


def contains_keywords(text, keywords):
    t = (text or '').lower()
    return any(k in t for k in keywords)


def decide_label(fields, synthetic_text):
    # priority keywords
    high_kw = ['niño', 'niños', 'niña', 'niñas', 'trabajo infantil', 'trabajo de niños', 'trata de personas']
    med_kw = ['incumplimiento', 'infraccion', 'inspeccion', 'denuncia']
    low_kw = []

    if contains_keywords(' '.join(str(v) for v in fields.values()), high_kw) or contains_keywords(synthetic_text, high_kw):
        return 'Alto', 'Keywords de trabajo infantil o similar'

    monto = parse_monto(fields)
    if monto is not None:
        # monto is in UTM units per extraction assumptions; heuristics thresholds
        if monto >= 100.0:
            return 'Alto', f'Multa alta: {monto} UTM'
        if monto >= 10.0:
            return 'Medio', f'Multa moderada: {monto} UTM'
        return 'Bajo', f'Multa baja: {monto} UTM'

    # fallback: check synthetic_text for sancion keyword
    if contains_keywords(synthetic_text, ['sancion', 'sancionado', 'mult']):
        return 'Medio', 'Texto indica sanción sin monto'

    # default low risk
    return 'Bajo', 'Sin indicios fuertes de riesgo'


def process_jsonl_file(path, out_fh):
    with open(path, 'r', encoding='utf8') as f:
        for line in f:
            if not line.strip():
                continue
            try:
                obj = json.loads(line)
            except Exception:
                continue
            fields = obj.get('fields', {}) or {}
            synthetic = obj.get('synthetic_text','') or ''
            label, reason = decide_label(fields, synthetic)
            gold = {
                'id': obj.get('id'),
                'source_pdf': obj.get('source_pdf'),
                'table_id': obj.get('table_id'),
                'row_number': obj.get('row_number'),
                'fields': fields,
                'synthetic_text': synthetic,
                'label': label,
                'reason': reason
            }
            out_fh.write(json.dumps(gold, ensure_ascii=False) + '\n')


def main():
    jsonl_dir = Path('ml/data/jsonl')
    out_path = Path('ml/data/gold.jsonl')
    out_path.parent.mkdir(parents=True, exist_ok=True)
    files = sorted(jsonl_dir.glob('*.jsonl'))
    if not files:
        print('No jsonl files found in', jsonl_dir)
        return
    with out_path.open('w', encoding='utf8') as out_fh:
        for p in files:
            print('Processing', p.name)
            process_jsonl_file(p, out_fh)
    print('Wrote gold records to', out_path)


if __name__ == '__main__':
    main()

