#!/usr/bin/env python3
"""
bootstrap_labels_ollama.py

Lee los chunks JSON en ml/chunks/ y usa la CLI de Ollama (ollama run) como fallback si HTTP no está disponible.
Genera labeled outputs en ml/labeled/ y un manifest.
"""
import os
import json
import subprocess
import shlex
import time
from tqdm import tqdm

CHUNKS_DIR = "ml/chunks"
OUT_DIR = "ml/labeled"
os.makedirs(OUT_DIR, exist_ok=True)

OLLAMA_MODEL = "llama3.1:8b"   # ajusta si tu modelo tiene otro nombre

PROMPT_TEMPLATE = """
Eres un experto legal chileno. Clasifica el nivel de riesgo regulatorio de la empresa según el siguiente fragmento de documento oficial. Sigue reglas objetivas (ver abajo).

Reglas de mapeo (usa sentido común):
- Multas altas / UF > 50, infracciones NNA, prácticas antisindicales, reincidencia: "alto"
- Multas medianas / UTM, faltas documentales graves: "medio"
- Sin sanciones y con políticas: "bajo"

Devuelve SOLO un JSON con claves:
- riesgo: "alto"|"medio"|"bajo"
- razones: [lista breve]
- confianza: numero entre 0.0 y 1.0

Fragmento:
<<<CHUNK>>>
"""


def call_ollama_cli(prompt_text, timeout=60):
    cmd = f"ollama run {OLLAMA_MODEL} --prompt {shlex.quote(prompt_text)} --quiet"
    try:
        res = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=timeout)
        out = res.stdout.strip()
        if not out:
            out = res.stderr.strip()
        return out
    except Exception as e:
        return f"ERROR_CALL:{e}"


files = sorted([f for f in os.listdir(CHUNKS_DIR) if f.endswith('.json')])
for f in tqdm(files):
    path = os.path.join(CHUNKS_DIR, f)
    with open(path, 'r', encoding='utf-8') as fi:
        obj = json.load(fi)
    prompt = PROMPT_TEMPLATE.replace('<<<CHUNK>>>', obj['text'][:4000])  # cap chunk size for prompt
    raw_out = call_ollama_cli(prompt)
    parsed = None
    try:
        import re
        m = re.search(r'(\{.*\})', raw_out, flags=re.S)
        if m:
            parsed = json.loads(m.group(1))
        else:
            parsed = {"raw": raw_out}
    except Exception as e:
        parsed = {"raw": raw_out, "parse_error": str(e)}
    out_obj = {
        "id": obj['id'],
        "source_pdf": obj['source_pdf'],
        "chunk_index": obj['chunk_index'],
        "text": obj['text'],
        "model_raw": raw_out,
        "label": parsed
    }
    with open(os.path.join(OUT_DIR, obj['id'] + '.json'), 'w', encoding='utf-8') as fo:
        json.dump(out_obj, fo, ensure_ascii=False, indent=2)
    time.sleep(0.2)

print('Done')

