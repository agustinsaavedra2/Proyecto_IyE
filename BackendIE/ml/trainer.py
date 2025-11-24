#!/usr/bin/env python3
"""
trainer.py

Entrenador sencillo que:
- Lee `ml/index.jsonl` (preparado con `prepare_dataset.py`)
- Si no hay etiquetas, intenta generar etiquetas usando Ollama (opcional) para crear dataset sintético
- Vectoriza texto (TfidfVectorizer)
- Entrena un clasificador (LogisticRegression o XGBoost)
- Guarda modelo y vectorizer en `ml/models/` usando joblib
- Produce un `ml/models/manifest.json` con metadata

Uso:
  python ml/trainer.py --index ml/index.jsonl --out-dir ml/models --label-method none|ollama|from_file --model xgb|logreg

"""

import argparse
import json
import os
from pathlib import Path
import joblib
import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression

try:
    import xgboost as xgb
except Exception:
    xgb = None

try:
    import requests
except Exception:
    requests = None


LABEL_MAP = {'bajo': 'Bajo', 'medio': 'Medio', 'alto': 'Alto'}
NUM_MAP = {'Bajo': 0, 'Medio': 1, 'Alto': 2}


def load_index(path: Path):
    rows = []
    with path.open('r', encoding='utf-8') as f:
        for line in f:
            line=line.strip()
            if not line: continue
            obj = json.loads(line)
            text = obj.get('text','')
            src = obj.get('meta',{}).get('source') if isinstance(obj.get('meta'), dict) else obj.get('meta', {}).get('source') if obj.get('meta') else None
            label = None
            # support labeled files where label is an object or string
            if 'label' in obj and obj.get('label'):
                lab = obj.get('label')
                if isinstance(lab, dict):
                    rv = lab.get('riesgo') or lab.get('risk')
                    if isinstance(rv, str):
                        rvn = rv.strip().lower()
                        label = LABEL_MAP.get(rvn)
                elif isinstance(lab, str):
                    rvn = lab.strip().lower()
                    label = LABEL_MAP.get(rvn)
            rows.append({'text':text,'source':src,'label':label})
    return pd.DataFrame(rows)


def label_with_ollama(df: pd.DataFrame):
    # This is a simple approach: call local Ollama for each text and ask to classify
    if requests is None:
        raise RuntimeError('requests not installed')
    url = 'http://localhost:11434/v1/complete'
    labels = []
    for i, row in df.iterrows():
        prompt = f"Clasifica el siguiente fragmento en riesgo: Bajo, Medio, Alto. Devuelve SOLO la etiqueta.\n\n{row['text'][:1000]}"
        try:
            r = requests.post(url, json={'model':'llama3.1:8b','prompt':prompt,'max_tokens':10}, timeout=30)
            if r.status_code==200:
                txt = r.json().get('text','').strip()
                lbl = txt.split('\n')[0].strip().capitalize()
                if lbl not in ['Bajo','Medio','Alto']:
                    lbl = 'Medio'
            else:
                lbl='Medio'
        except Exception:
            lbl='Medio'
        labels.append(lbl)
    return labels


def train(df: pd.DataFrame, labels, out_dir: Path, model_type='logreg'):
    out_dir.mkdir(parents=True, exist_ok=True)
    vect = TfidfVectorizer(max_features=20000, ngram_range=(1,2))
    X = vect.fit_transform(df['text'].fillna(''))
    y = np.array([NUM_MAP.get(l,1) for l in labels])
    try:
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42,
            stratify=y if len(set(y))>1 else None
        )
    except ValueError:
        # Fallback for very small datasets or when stratify fails
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=None
        )

    if model_type=='xgb' and xgb is not None:
        model = xgb.XGBClassifier(use_label_encoder=False, eval_metric='mlogloss')
        model.fit(X_train, y_train)
    else:
        model = LogisticRegression(max_iter=1000, multi_class='multinomial')
        model.fit(X_train, y_train)

    # eval
    if X_test.shape[0] > 0:
        acc = model.score(X_test, y_test)
        note = None
    else:
        # No test set (too few samples) — report training accuracy instead and mark note
        try:
            acc = model.score(X_train, y_train)
        except Exception:
            acc = 0.0
        note = 'training_accuracy_no_test_set'

    joblib.dump(vect, out_dir/'vectorizer.joblib')
    joblib.dump(model, out_dir/'model.joblib')
    manifest = {
        'model_type': model_type,
        'accuracy': float(acc),
        'n_samples': int(len(df)),
    }
    if 'note' in locals() and note:
        manifest['note'] = note
    with (out_dir/'manifest.json').open('w', encoding='utf-8') as f:
        json.dump(manifest, f, indent=2)
    return manifest


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--index', default='ml/index.jsonl')
    p.add_argument('--out-dir', default='ml/models')
    p.add_argument('--label-method', choices=['none','ollama','from_file'], default='none', help='from_file will use label field inside jsonl if present')
    p.add_argument('--model', choices=['logreg','xgb'], default='logreg')
    args = p.parse_args()

    index = Path(args.index)
    if not index.exists():
        print('index file not found:', index)
        return
    df = load_index(index)
    print('Loaded', len(df), 'chunks')

    labels = None
    if args.label_method == 'from_file':
        # use labels present in file if available
        labels = df['label'].tolist()
        # If no labels found (all None), fall back to heuristic
        if all(l is None for l in labels):
            labels = None

    if labels is None:
        if args.label_method == 'ollama':
            labels = label_with_ollama(df)
        else:
            # Heuristic labeling: if filename or source contains keywords -> set label
            def heuristic_label(src):
                s = (src or '').lower()
                if any(k in s for k in ['sancion','multa','resolucion','multad']):
                    return 'Alto'
                if any(k in s for k in ['incumplimiento','infraccion','inspeccion']):
                    return 'Medio'
                return 'Bajo'
            labels = [heuristic_label(x) for x in df['source']]

    manifest = train(df, labels, Path(args.out_dir), model_type=args.model)
    print('Training completed:', manifest)

if __name__=='__main__':
    main()
