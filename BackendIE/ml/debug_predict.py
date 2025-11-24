#!/usr/bin/env python3
import traceback
import joblib
from pathlib import Path
import json

MODEL_DIR = Path(__file__).parent / 'models'
VECT_FILE = MODEL_DIR / 'vectorizer.joblib'
MODEL_FILE = MODEL_DIR / 'model.joblib'

print('Model dir:', MODEL_DIR)
try:
    print('Exists vect:', VECT_FILE.exists(), 'size:', VECT_FILE.stat().st_size if VECT_FILE.exists() else None)
    print('Exists model:', MODEL_FILE.exists(), 'size:', MODEL_FILE.stat().st_size if MODEL_FILE.exists() else None)
except Exception as e:
    print('Error checking files:', e)

try:
    vect = joblib.load(str(VECT_FILE))
    model = joblib.load(str(MODEL_FILE))
    print('Loaded vectorizer type:', type(vect))
    print('Loaded model type:', type(model))
    sample = 'Empresa con historial de multas por trabajo infantil y sanciones repetidas'
    X = vect.transform([sample])
    print('Transformed shape:', X.shape)
    has_proba = hasattr(model, 'predict_proba')
    print('Has predict_proba:', has_proba)
    if has_proba:
        probs = model.predict_proba(X)[0].tolist()
        pred = int(model.predict(X)[0])
    else:
        pred = int(model.predict(X)[0])
        probs = [0.0,0.0,0.0]
        probs[pred] = 1.0
    out = {'label_index': pred, 'label': ['Bajo','Medio','Alto'][pred], 'probabilities': probs}
    print('PREDICTION:', json.dumps(out, ensure_ascii=False))
except Exception as e:
    print('Error during prediction:')
    traceback.print_exc()

