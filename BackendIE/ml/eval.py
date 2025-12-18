#!/usr/bin/env python3
"""
ml/eval.py
Evalúa un modelo entrenado dado un index.jsonl con labels.
Salida: imprime accuracy y F1-macro.
"""
import argparse
import json
from pathlib import Path
import joblib
import numpy as np
from sklearn.metrics import accuracy_score, f1_score, confusion_matrix

LABELS = ['Bajo','Medio','Alto']
NUM_MAP = {'Bajo':0,'Medio':1,'Alto':2}


def load_index(path: Path):
    rows = []
    with path.open('r', encoding='utf-8') as f:
        for line in f:
            line=line.strip()
            if not line: continue
            obj = json.loads(line)
            text = obj.get('text','')
            label = None
            if 'label' in obj and obj.get('label'):
                lab = obj.get('label')
                if isinstance(lab, dict):
                    rv = lab.get('riesgo') or lab.get('risk')
                    if isinstance(rv, str):
                        label = rv.strip().capitalize()
                elif isinstance(lab, str):
                    label = lab.strip().capitalize()
            rows.append({'text':text,'label':label})
    return rows


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--index', default='ml/index.jsonl')
    p.add_argument('--model-dir', default='ml/models')
    args = p.parse_args()

    index = Path(args.index)
    model_dir = Path(args.model_dir)
    if not index.exists():
        print('index file not found:', index)
        return
    if not model_dir.exists():
        print('model dir not found:', model_dir)
        return

    rows = load_index(index)
    texts = [r['text'] for r in rows if r['label'] is not None]
    labels = [r['label'] for r in rows if r['label'] is not None]
    if len(texts)==0:
        print('No labeled examples found in index.jsonl to evaluate')
        return

    vect = joblib.load(str(model_dir / 'vectorizer.joblib'))
    model = joblib.load(str(model_dir / 'model.joblib'))
    X = vect.transform(texts)
    y_true = np.array([NUM_MAP.get(l,1) for l in labels])
    y_pred = model.predict(X)

    acc = accuracy_score(y_true, y_pred)
    f1 = f1_score(y_true, y_pred, average='macro')
    cm = confusion_matrix(y_true, y_pred)

    print('n_samples_evaluated:', len(y_true))
    print('accuracy:', acc)
    print('f1_macro:', f1)
    print('confusion_matrix:\n', cm)

if __name__=='__main__':
    main()

