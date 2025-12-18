#!/usr/bin/env python3
"""
Convierte PDFs del directorio `ml/dataset` en ejemplos JSONL para fine-tuning de LLM.
Cada ejemplo es un objeto: {"prompt": "...", "completion": "..."}
Estrategia simple: extrae texto por página y crea prompts tipo:
  Prompt: "Genera una política breve basada en el siguiente fragmento legal: <texto de la sección>"
  Completion: "::POLITICA::\n::titulo:: ... ::contenido:: ... ::END_POLITICA::"

Esto es un punto de partida; ajusta las plantillas según lo que quieras fine-tunear.
"""
import argparse
import json
from pathlib import Path
import pdfplumber


def extract_texts_from_pdf(pdf_path: Path):
    texts = []
    with pdfplumber.open(str(pdf_path)) as pdf:
        for i, page in enumerate(pdf.pages, start=1):
            t = page.extract_text() or ''
            if t.strip():
                texts.append({'page': i, 'text': t})
    return texts


def make_examples_from_pdf(pdf_path: Path, max_chars=2000):
    examples = []
    texts = extract_texts_from_pdf(pdf_path)
    for t in texts:
        snippet = t['text'].strip()
        if len(snippet) > max_chars:
            snippet = snippet[:max_chars]
        prompt = f"Genera una política de cumplimiento sintética basada en el siguiente fragmento de normativa o guía:\n{snippet}\n\nInstrucciones: Devuelve SOLO el bloque con el siguiente formato EXACTO:\n::POLITICA::\n::titulo:: <titulo>\n::contenido:: <texto de la politica>\n::END_POLITICA::"
        completion = "::POLITICA::\n::titulo:: Politica generada\n::contenido:: [REMPLAZAR POR SALIDA DEL MODELO]\n::END_POLITICA::"
        examples.append({'prompt': prompt, 'completion': completion})
    return examples


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--dataset-dir', default='ml/dataset')
    p.add_argument('--out', default='llm_finetune/dataset.jsonl')
    p.add_argument('--max-chars', type=int, default=2000)
    args = p.parse_args()

    src = Path(args.dataset_dir)
    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)

    all_examples = []
    for pdf in sorted(src.glob('*.pdf')):
        ex = make_examples_from_pdf(pdf, max_chars=args.max_chars)
        all_examples.extend(ex)
        print(f"Processed {pdf.name}: {len(ex)} examples")

    with out.open('w', encoding='utf-8') as f:
        for e in all_examples:
            f.write(json.dumps(e, ensure_ascii=False) + '\n')
    print('Wrote', out, 'n=', len(all_examples))

if __name__ == '__main__':
    main()

