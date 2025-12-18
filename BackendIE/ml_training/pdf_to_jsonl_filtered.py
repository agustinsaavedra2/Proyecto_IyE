#!/usr/bin/env python3
"""
ml_training/pdf_to_jsonl_filtered.py
Genera llm_finetune/dataset_filtered.jsonl procesando solo los PDFs
que NO están referenciados en ml/index.jsonl (meta.source).
Salida: llm_finetune/dataset_filtered.jsonl

Uso: python ml_training/pdf_to_jsonl_filtered.py
"""
from pathlib import Path
import json

BASE = Path(__file__).parent.parent
INDEX = BASE / 'ml' / 'index.jsonl'
DATASET_DIR = BASE / 'ml' / 'dataset'
OUT = BASE / 'llm_finetune' / 'dataset_filtered.jsonl'
OUT.parent.mkdir(parents=True, exist_ok=True)

# PDFs que sabemos que se usaron para entrenar el clasificador y deben EXCLUIRSE
EXCLUDE_STEMS = {
    'Empresas_y_Organizaciones_Sindicales_sancionadas_por_Prácticas_Antisindicales_primer_semestre_2025'.replace('á','a').replace('í','i').replace('ó','o').replace('é','e'),
    'Listado_de_empresas_multadas_por_infracciones_relacionadas_al_trabajo_de_niños_niñas_Y_adolescentes_(NNA)_primer_semestre_2025'.replace('ó','o').replace('ñ','n').replace('á','a').replace('é','e').replace('í','i')
}

# Leer fuentes usadas en index.jsonl
used_basenames = set()
if INDEX.exists():
    for line in INDEX.read_text(encoding='utf-8').splitlines():
        if not line.strip():
            continue
        try:
            obj = json.loads(line)
            meta = obj.get('meta') or {}
            src = meta.get('source')
            if isinstance(src, str) and src.strip():
                used_basenames.add(Path(src).stem)
        except Exception:
            continue

# Unir automaticamente EXCLUDE_STEMS con lo que provenga del índice
# Normalizamos stems simples (sin acentos) para robustez al comparar nombres de archivos en el FS
def normalize_stem(s: str):
    return s.replace('á','a').replace('Á','A').replace('é','e').replace('É','E').replace('í','i').replace('Í','I').replace('ó','o').replace('Ó','O').replace('ú','u').replace('Ú','U').replace('ñ','n').replace('Ñ','N')

used_basenames_norm = {normalize_stem(x) for x in used_basenames}
EXCLUDE_STEMS = {normalize_stem(x) for x in EXCLUDE_STEMS}

pdfs = sorted([p for p in DATASET_DIR.glob('*.pdf')])
# select PDFs not used -> excluir si stem (normalizado) está en used_basenames_norm o EXCLUDE_STEMS
to_process = [p for p in pdfs if normalize_stem(p.stem) not in used_basenames_norm and normalize_stem(p.stem) not in EXCLUDE_STEMS]

print(f"Found {len(pdfs)} PDFs in {DATASET_DIR}")
print(f"PDFs referenced in index.jsonl: {len(used_basenames)}")
if used_basenames_norm:
    print("Referenced samples (stems, normalized):", sorted(list(used_basenames_norm)) )
print(f"Explicit exclude stems (normalized): {sorted(list(EXCLUDE_STEMS))}")
print(f"PDFs to process (not used): {len(to_process)}")

# Try to use pdfplumber, else PyPDF2
use_pdfplumber = False
try:
    import pdfplumber
    use_pdfplumber = True
    print('Using pdfplumber for extraction')
except Exception:
    try:
        from PyPDF2 import PdfReader
        print('Using PyPDF2 for extraction')
    except Exception:
        raise RuntimeError('No PDF extraction library available. Install pdfplumber or PyPDF2')

max_chars = 2000
examples_total = 0
processed_files = []
with OUT.open('w', encoding='utf-8') as out:
    for pdf in to_process:
        texts = []
        try:
            if use_pdfplumber:
                import pdfplumber
                with pdfplumber.open(str(pdf)) as doc:
                    for i, page in enumerate(doc.pages, start=1):
                        t = page.extract_text() or ''
                        if t.strip():
                            texts.append({'page': i, 'text': t})
            else:
                from PyPDF2 import PdfReader
                reader = PdfReader(str(pdf))
                for i, page in enumerate(reader.pages, start=1):
                    try:
                        t = page.extract_text() or ''
                    except Exception:
                        t = ''
                    if t.strip():
                        texts.append({'page': i, 'text': t})
        except Exception as e:
            print(f'Failed to read {pdf.name}: {e}')
            continue

        for t in texts:
            snippet = t['text'].strip()
            if len(snippet) > max_chars:
                snippet = snippet[:max_chars]
            prompt = (
                f"Genera una política de cumplimiento sintética basada en el siguiente fragmento de normativa o guía:\n{snippet}\n\n"
                "Instrucciones: Devuelve SOLO el bloque con el siguiente formato EXACTO:\n"
                "::POLITICA::\n::titulo:: <titulo>\n::contenido:: <texto de la politica>\n::END_POLITICA::"
            )
            completion = "::POLITICA::\n::titulo:: Politica generada\n::contenido:: [REMPLAZAR POR SALIDA_DEL_MODELO]\n::END_POLITICA::"
            ex = {
                'prompt': prompt,
                'completion': completion,
                'meta': {'source_pdf': pdf.name, 'page': t['page']}
            }
            out.write(json.dumps(ex, ensure_ascii=False) + '\n')
            examples_total += 1
        if texts:
            processed_files.append({'file': pdf.name, 'pages': len(texts), 'examples': len(texts)})

print(f'Wrote {OUT} with {examples_total} examples from {len(processed_files)} files')
for pf in processed_files:
    print(f" - {pf['file']}: pages={pf['pages']} examples={pf['examples']}")

print('Done')

