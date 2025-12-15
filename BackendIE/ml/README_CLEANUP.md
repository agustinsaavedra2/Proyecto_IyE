Resumen: limpieza de la carpeta ml y generación de datos de ejemplo

Este directorio contiene scripts para extraer tablas desde PDFs en `ml/dataset`, convertirlas a CSV/JSONL, generar chunks y regenerar el índice.

Estructura propuesta:
- ml/data/raw/  -> PDFs originales (si prefieres mantener copia aquí)
- ml/data/csv/  -> CSVs extraídos por tabla
- ml/data/jsonl/ -> JSONL por tabla (una línea por fila)
- ml/chunks/    -> Chunks JSON + manifest.json
- ml/index.jsonl -> Índice regenerado
- ml/_cleanup_backup/ -> respaldo temporal de archivos movidos
- ml/scripts/ -> scripts: extract_tables.py, tables_to_jsonl.py, generate_chunks.py, regenerate_index.py

Requisitos python (sugeridos):
- pdfplumber
- pandas
- tqdm (opcional)

Comandos rápidos (PowerShell):
1. Crear entorno
   python -m venv .venv; .\.venv\Scripts\Activate.ps1
   pip install pdfplumber pandas

2. Extraer tablas a CSV
   python .\ml\scripts\extract_tables.py --pdf "ml\dataset\Empresas_y_Organizaciones_Sindicales_sancionadas_por_Prácticas_Antisindicales_primer_semestre_2025.pdf" --out-dir ml\data\csv
   python .\ml\scripts\extract_tables.py --pdf "ml\dataset\Listado_de_empresas_multadas_por_infracciones_relacionadas_al_trabajo_de_niños_niñas_y_adolescentes_(NNA)_primer_semestre_2025.pdf" --out-dir ml\data\csv

3. Convertir CSVs a JSONL
   python .\ml\scripts\tables_to_jsonl.py --csv-dir ml\data\csv --out ml\data\jsonl

4. Generar chunks y manifest
   python .\ml\scripts\generate_chunks.py --jsonl-dir ml\data\jsonl --chunks-dir ml\chunks --manifest ml\chunks\manifest.json

5. Regenerar índice
   python .\ml\scripts\regenerate_index.py --manifest ml\chunks\manifest.json --out ml\index.jsonl

Notas:
- Los scripts usan heurísticas sencillas y están pensados para crear datos de ejemplo reproducibles. Si los PDFs son escaneados necesitarás OCR (tesseract) y ajustar la extracción.
- Hacer backup antes de mover/borrar archivos reales: md ml\_cleanup_backup && move ml\index_labeled*.jsonl ml\_cleanup_backup\


