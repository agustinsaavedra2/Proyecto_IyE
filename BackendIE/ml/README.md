Dataset preparation for ML

Place public Chilean normative documents and sanction cases into `ml/dataset/`.
Recommended sources:

- Biblioteca del Congreso Nacional de Chile: https://www.bcn.cl/ (laws and decrees)
- Biblioteca Jurídica de Chile: https://www.biblioteca.juridica.org/ (legal repos)
- Dirección del Trabajo (Chile): https://www.dt.gob.cl/ (cases, sanctions, resolutions)
- Ministerio de Salud: https://www.minsal.cl/ (sanitary regulations)

Options to populate the dataset:

- Manual download: visit the above sites and download PDFs into `ml/dataset/`.
- URL list: create `ml/urls.txt` with PDF URLs (one per line) and run `prepare_dataset.py --download-urls` to fetch PDFs.

Suggested flow:
1) Collect ~500-2000 documents (laws, regs, sanction reports). Start smaller (50-200) to iterate quickly.
2) Run: python ml/prepare_dataset.py --dataset-dir ml/dataset --out ml/index.jsonl --chunk-size 1000 --embed
3) Use `ml/index.jsonl` as input for RAG or for fine-tuning prompts.

Notes on cost and scale:
- Storing raw PDFs locally is fine; embeddings are heavier. If you want to save space, compute embeddings once and store vector store (pgvector or disk) and remove raw PDFs.
- You can host documents in S3 and read them on demand; for local dev keep them in `ml/dataset/`.

