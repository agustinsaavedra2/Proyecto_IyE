"""
ml/explain.py
Módulo simple de explicabilidad para modelos de regresión logística entrenados con TfidfVectorizer.
Devuelve contribuciones token->score aproximadas (coeficiente * tfidf)
"""
from pathlib import Path
import joblib
import numpy as np

LABELS = ['Bajo','Medio','Alto']

BASE_DIR = Path(__file__).parent
MODELS_AUG_DIR = BASE_DIR / 'models_augmented'
MODELS_DIR = BASE_DIR / 'models'


def get_model_dir_for_tenant(tenant_id: str = None) -> Path:
    candidates = []
    if tenant_id:
        candidates.extend([
            MODELS_AUG_DIR / tenant_id,
            MODELS_DIR / tenant_id,
        ])
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
        raise FileNotFoundError(f'Model artifacts not found for tenant {tenant_id}. Searched under: {MODELS_AUG_DIR} and {MODELS_DIR}.')
    vect = joblib.load(str(model_dir / 'vectorizer.joblib'))
    model = joblib.load(str(model_dir / 'model.joblib'))
    return vect, model, model_dir


def explain_text(text, top_k=10, tenant_id: str = None):
    vect, model, model_dir = load_artifacts(tenant_id)
    # Solo soporte directo para LogisticRegression con coef_ y vocab
    if not hasattr(vect, 'get_feature_names_out'):
        raise RuntimeError('Vectorizer does not support get_feature_names_out')
    feat_names = vect.get_feature_names_out()
    x = vect.transform([text])
    if hasattr(model, 'coef_'):
        coefs = model.coef_  # shape (n_classes, n_features)
        # calcular score por token para la clase predicha
        pred = int(model.predict(x)[0])
        class_coefs = coefs[pred]
        # obtener tfidf values
        x_data = x.toarray()[0]
        contrib = class_coefs * x_data
        # tomar top_k tokens
        idxs = np.argsort(contrib)[-top_k:][::-1]
        tokens = [(feat_names[i], float(contrib[i])) for i in idxs if contrib[i] != 0.0]
        return {
            'predicted_class': LABELS[pred],
            'tokens': tokens,
            '_model_path': str(model_dir.absolute())
        }
    else:
        raise RuntimeError('Model does not expose coef_ (not a linear model)')
