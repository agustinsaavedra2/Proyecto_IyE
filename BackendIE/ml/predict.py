#!/usr/bin/env python3
"""
predict.py
Carga el vectorizer y modelo (joblib) generado por trainer.py y predice la clase (Bajo/Medio/Alto)
Uso:
  echo "texto..." | python ml/predict.py
  python ml/predict.py "texto..."
Salida: JSON con keys: label (Bajo/Medio/Alto), label_index, probabilities (list)

Este archivo ahora busca primero `ml/models_augmented/` (modelo mejorado) y si no existe, usa `ml/models/`.
"""

import sys
import json
from pathlib import Path
import joblib
import numpy as np

LABELS = ['Bajo','Medio','Alto']

# Nuevo: preferir models_augmented si está presente
BASE_DIR = Path(__file__).parent
MODELS_AUG_DIR = BASE_DIR / 'models_augmented'
MODELS_DIR = BASE_DIR / 'models'


def get_model_dir_for_tenant(tenant_id: str = None) -> Path:
    """Devuelve el directorio de artefactos a usar según tenant_id.
    Búsqueda en orden:
      1. models_augmented/{tenant_id}
      2. models_augmented/global
      3. models/{tenant_id}
      4. models/global
      5. models_augmented (root)
      6. models (root)
    """
    candidates = []
    if tenant_id:
        candidates.extend([
            MODELS_AUG_DIR / tenant_id,
            MODELS_DIR / tenant_id,
        ])
    # global fallback inside augmented and models
    candidates.extend([
        MODELS_AUG_DIR / 'global',
        MODELS_DIR / 'global',
        MODELS_AUG_DIR,
        MODELS_DIR,
    ])
    for c in candidates:
        if (c / 'model.joblib').exists() and (c / 'vectorizer.joblib').exists():
            return c
    return None


def load_artifacts(tenant_id: str = None):
    model_dir = get_model_dir_for_tenant(tenant_id)
    if model_dir is None:
        raise FileNotFoundError(f'Model artifacts not found for tenant {tenant_id}. Searched under: {MODELS_AUG_DIR} and {MODELS_DIR}. Run ml/trainer.py first or place artifacts in one of those directories')
    vect_file = model_dir / 'vectorizer.joblib'
    model_file = model_dir / 'model.joblib'
    vect = joblib.load(str(vect_file))
    model = joblib.load(str(model_file))
    return vect, model, model_dir


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


# Wrapper conveniente que carga artefactos internamente
def predict_text(text, tenant_id: str = None):
    """Predice usando los artefactos guardados en ml/models_augmented o ml/models. Devuelve diccionario.
    Lanza FileNotFoundError si faltan artefactos.
    Añade key '_model_path' en la respuesta con la carpeta usada.
    """
    vect, model, model_dir = load_artifacts(tenant_id)
    res = predict(text, vect, model)
    res['_model_path'] = str(model_dir.absolute())
    return res


def main():
    if len(sys.argv) > 1:
        text = ' '.join(sys.argv[1:])
    else:
        text = sys.stdin.read().strip()

    if not text:
        print(json.dumps({'error':'no input text provided'}))
        sys.exit(2)

    vect, model, model_dir = load_artifacts()
    res = predict(text, vect, model)
    res['_model_path'] = str(model_dir.absolute())
    print(json.dumps(res, ensure_ascii=False))


if __name__ == '__main__':
    main()
