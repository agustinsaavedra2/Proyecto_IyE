#!/usr/bin/env python3
"""
predict.py
Carga el vectorizer y modelo (joblib) generado por trainer.py y predice la clase (Bajo/Medio/Alto)
Uso:
  echo "texto..." | python ml/predict.py
  python ml/predict.py "texto..."
Salida: JSON con keys: label (Bajo/Medio/Alto), label_index, probabilities (list)
"""

import sys
import json
from pathlib import Path
import joblib
import numpy as np

LABELS = ['Bajo','Medio','Alto']

MODEL_DIR = Path(__file__).parent / 'models'
VECT_FILE = MODEL_DIR / 'vectorizer.joblib'
MODEL_FILE = MODEL_DIR / 'model.joblib'


def load_artifacts():
    if not VECT_FILE.exists() or not MODEL_FILE.exists():
        raise FileNotFoundError('Model artifacts not found in ml/models. Run ml/trainer.py first')
    vect = joblib.load(str(VECT_FILE))
    model = joblib.load(str(MODEL_FILE))
    return vect, model


def predict(text, vect, model):
    X = vect.transform([text])
    if hasattr(model, 'predict_proba'):
        probs = model.predict_proba(X)[0].tolist()
    else:
        # some models may not support predict_proba
        pred = model.predict(X)[0]
        probs = [0.0]*len(LABELS)
        probs[int(pred)] = 1.0
    pred_idx = int(model.predict(X)[0])
    return {
        'label': LABELS[pred_idx],
        'label_index': pred_idx,
        'probabilities': probs
    }


def main():
    if len(sys.argv) > 1:
        text = ' '.join(sys.argv[1:])
    else:
        text = sys.stdin.read().strip()

    if not text:
        print(json.dumps({'error':'no input text provided'}))
        sys.exit(2)

    vect, model = load_artifacts()
    res = predict(text, vect, model)
    print(json.dumps(res, ensure_ascii=False))


if __name__ == '__main__':
    main()

