#!/usr/bin/env python3
"""
label_with_ollama.py

Lee ml/index.jsonl y para cada chunk (o hasta --limit) llama a Ollama para obtener un JSON con campos estructurados:
  {"riesgo":"alto|medio|bajo","razones":[...],"multa_monto":123.0,"categoria_infraccion":"...","reincidencia":true,"sector":"...","tamano_empresa":"pequena|mediana|grande"}

Salida:
 - ml/index_labeled.jsonl  (cada línea: original record + field 'label' with the parsed JSON)
 - ml/labels_to_review.csv (CSV para validación humana con columnas: id,text,label,razones,multa_monto,categoria_infraccion,reincidencia,sector,tamano)

Uso:
  python ml/label_with_ollama.py --limit 200

Nota: requiere requests. Ollama local expected at http://localhost:11434/v1/complete (model llama3.1:8b)

"""
import argparse
import json
import time
from pathlib import Path
import requests
import subprocess, shlex

OLLAMA_URL = 'http://localhost:11434/v1/complete'
MODEL = 'llama3.1:8b'

PROMPT_TEMPLATE = (
    "Eres un experto legal en Chile.\n\n"
    "Clasifica el nivel de riesgo regulatorio de la empresa según la siguiente información REAL.\n\n"
    "Reglas:\n"
    "- Multas altas o graves = Alto\n"
    "- Trabajo infantil (NNA), antisindicalidad, higiene crítica = Alto\n"
    "- Multas bajas = Medio\n"
    "- Documentación incompleta = Medio\n"
    "- Sin sanciones y con políticas = Bajo\n\n"
    "Devuelve solo un JSON compacto con estas claves (ejemplo abajo):\n"
    "{{\"riesgo\": \"alto\", \"razones\": [\"multas graves\",\"reincidencia\"], \"multa_monto\": 750.0, \"categoria_infraccion\":\"trabajo de menores\", \"reincidencia\": true, \"sector\":\"alimentacion\", \"tamano_empresa\":\"pequena\"}}\n\n"
    "Texto analizado:\n<<<\n{text}\n>>>\n\n"
    "Si no puedes inferir un campo, pon null o [] según corresponda.\n"
    "Responde SOLO con el JSON."
)


def call_ollama(text, timeout=60, max_retries=2):
    """Try several common Ollama-style endpoints and return the text response or raise an exception.
    Adds basic retries and detailed error logging for troubleshooting.
    """
    endpoints = ["/v1/complete", "/v1/completions", "/v1/chat/completions", "/v1/generate"]
    headers = {"Content-Type": "application/json"}
    last_err = None
    for ep in endpoints:
        url = OLLAMA_URL.replace('/v1/complete', ep) if OLLAMA_URL.endswith('/v1/complete') else OLLAMA_URL.rstrip('/') + ep
        payload = None
        # adapt payload shape for common endpoints
        if ep == '/v1/chat/completions':
            payload = {
                'model': MODEL,
                'messages': [{'role': 'user', 'content': PROMPT_TEMPLATE.format(text=text[:4000])}],
                'max_tokens': 300
            }
        else:
            payload = {
                'model': MODEL,
                'prompt': PROMPT_TEMPLATE.format(text=text[:4000]),
                'max_tokens': 300
            }

        for attempt in range(max_retries + 1):
            try:
                r = requests.post(url, json=payload, timeout=timeout, headers=headers)
                # if we get a non-200, capture body for debugging
                if r.status_code != 200:
                    last_err = f"HTTP {r.status_code} from {url}: {r.text[:1000]}"
                    # break to try next endpoint
                    break
                data = r.json()
                # support various response shapes
                if isinstance(data, dict):
                    if 'text' in data:
                        return data['text']
                    if 'result' in data and isinstance(data['result'], dict) and 'text' in data['result']:
                        return data['result']['text']
                    if 'choices' in data and isinstance(data['choices'], list) and len(data['choices'])>0:
                        c = data['choices'][0]
                        if isinstance(c, dict) and 'message' in c:
                            return c['message'].get('content','')
                        if isinstance(c, dict) and 'text' in c:
                            return c.get('text','')
                # fallback to raw text
                return r.text
            except Exception as e:
                last_err = str(e)
                # small backoff
                time.sleep(0.5 + attempt * 0.5)
                continue
    # if we get here all endpoints failed
    raise RuntimeError(f"Ollama requests failed. Last error: {last_err}")


def call_ollama_cli(text, timeout=60):
    # Use ollama CLI as fallback
    cmd = f"ollama run {MODEL} --prompt {shlex.quote(PROMPT_TEMPLATE.format(text=text[:4000]))} --quiet"
    try:
        res = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=timeout)
        out = res.stdout.strip()
        if not out:
            out = res.stderr.strip()
        return out
    except Exception as e:
        return None


def check_ollama_available():
    """Check /v1/models to ensure Ollama is reachable and list available models. Returns list or raises."""
    try:
        url = OLLAMA_URL.replace('/v1/complete', '/v1/models') if OLLAMA_URL.endswith('/v1/complete') else OLLAMA_URL.rstrip('/') + '/v1/models'
        r = requests.get(url, timeout=5)
        r.raise_for_status()
        data = r.json()
        models = []
        if isinstance(data, dict) and 'data' in data:
            for item in data.get('data', []):
                models.append(item.get('id'))
        return models
    except Exception as e:
        raise RuntimeError(f"Ollama not available at {url}: {e}")


def parse_json_from_text(text):
    if not text: return None
    s = text.strip()
    # Find first { ... }
    start = s.find('{')
    end = s.rfind('}')
    if start == -1 or end == -1 or end<=start:
        return None
    try:
        j = json.loads(s[start:end+1])
        return j
    except Exception:
        # try minor fixes: single quotes to double
        try:
            s2 = s[start:end+1].replace("'", '"')
            j = json.loads(s2)
            return j
        except Exception:
            return None


# --- Nueva función: normaliza la etiqueta para CSV y evita None en listas -----
def sanitize_label_for_csv(label):
    """Return a cleaned dict with safe string lists for 'razones' to avoid CSV join errors."""
    if not isinstance(label, dict):
        return {
            'riesgo': None,
            'razones': [],
            'multa_monto': None,
            'categoria_infraccion': None,
            'reincidencia': None,
            'sector': None,
            'tamano_empresa': None,
        }
    # razones: ensure list of strings
    razones = label.get('razones') or []
    if isinstance(razones, (list, tuple)):
        razones_clean = [str(r) for r in razones if r is not None]
    else:
        # single value -> convert to string
        razones_clean = [str(razones)] if razones is not None else []

    # tamaño puede venir con claves distintas
    tamano = label.get('tamano_empresa') or label.get('tamano') or None

    return {
        'riesgo': label.get('riesgo'),
        'razones': razones_clean,
        'multa_monto': label.get('multa_monto'),
        'categoria_infraccion': label.get('categoria_infraccion'),
        'reincidencia': label.get('reincidencia'),
        'sector': label.get('sector'),
        'tamano_empresa': tamano,
    }


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--index', default='ml/index.jsonl')
    p.add_argument('--out', default='ml/index_labeled.jsonl')
    p.add_argument('--csv', default='ml/labels_to_review.csv')
    p.add_argument('--start', type=int, default=0)
    p.add_argument('--limit', type=int, default=0)
    p.add_argument('--sleep', type=float, default=0.2)
    args = p.parse_args()

    idx = Path(args.index)
    out = Path(args.out)
    csvf = Path(args.csv)
    if not idx.exists():
        print('index file missing:', idx)
        return

    out.parent.mkdir(parents=True, exist_ok=True)

    total = 0
    written = 0
    import csv
    with idx.open('r', encoding='utf-8') as fin, out.open('a', encoding='utf-8') as fout, csvf.open('a', newline='', encoding='utf-8') as cfs:
        writer = csv.writer(cfs)
        # write header if empty
        if cfs.tell() == 0:
            writer.writerow(['id','source','text_sample','riesgo','razones','multa_monto','categoria_infraccion','reincidencia','sector','tamano'])

        for i, line in enumerate(fin):
            if i < args.start:
                continue
            if args.limit and written >= args.limit:
                break
            line=line.strip()
            if not line:
                continue
            try:
                rec = json.loads(line)
            except Exception:
                continue
            total += 1
            text = rec.get('text','')
            src = rec.get('meta',{}).get('source','')
            # Check Ollama availability before the first call
            if written == 0:
                try:
                    models = check_ollama_available()
                    print(f"Ollama reachable, models: {models}")
                except Exception as e:
                    print(f"Ollama availability check failed: {e}")
                    return
            # Call Ollama
            try:
                resp_text = call_ollama(text)
            except Exception as e:
                print(f'[{i}] Ollama call failed: {e}')
                # try CLI fallback
                print(f'[{i}] Trying Ollama CLI fallback...')
                try:
                    resp_text = call_ollama_cli(text)
                except Exception as e2:
                    print(f'[{i}] Ollama CLI fallback failed: {e2}')
                    time.sleep(args.sleep)
                    continue
                if not resp_text:
                    print(f'[{i}] Ollama CLI returned empty')
                    time.sleep(args.sleep)
                    continue
            # proceed with resp_text
            parsed = parse_json_from_text(resp_text)
            # keep original parsed in output JSON, but use sanitized for CSV
            rec_out = rec.copy()
            rec_out['label'] = parsed if parsed is not None else {'riesgo': None, 'razones': [], 'multa_monto': None, 'categoria_infraccion': None, 'reincidencia': None, 'sector': None, 'tamano_empresa': None}
            fout.write(json.dumps(rec_out, ensure_ascii=False) + '\n')

            safe_label = sanitize_label_for_csv(parsed if parsed is not None else rec_out['label'])
            # write CSV row for review (short sample text)
            sample = text.strip().replace('\n',' ')[:300]
            writer.writerow([rec.get('id'), src, sample, safe_label.get('riesgo'), ';'.join(safe_label.get('razones') or []), safe_label.get('multa_monto'), safe_label.get('categoria_infraccion'), safe_label.get('reincidencia'), safe_label.get('sector'), safe_label.get('tamano_empresa')])
            written += 1
            if written % 10 == 0:
                print(f'Processed {written} items')
            time.sleep(args.sleep)

    print(f'Done. processed {written} items (read {total})')

if __name__=='__main__':
    main()
