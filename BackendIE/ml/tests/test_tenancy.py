import os
import joblib
from pathlib import Path
from sklearn.linear_model import LogisticRegression
from sklearn.feature_extraction.text import CountVectorizer
import numpy as np

import pytest

import ml.predict as predict_module
import ml.explain as explain_module


def make_dummy_model_and_vectorizer(folder: Path):
    folder.mkdir(parents=True, exist_ok=True)
    vect = CountVectorizer()
    # fit on small corpus
    docs = ["bajo riesgo ejemplo", "alto riesgo multa sancion", "medio riesgo" ]
    X = vect.fit_transform(docs)
    y = np.array([0, 2, 1])
    model = LogisticRegression(max_iter=200)
    model.fit(X, y)
    joblib.dump(vect, str(folder / 'vectorizer.joblib'))
    joblib.dump(model, str(folder / 'model.joblib'))


def test_tenancy_selection_and_fallback(tmp_path, monkeypatch):
    # preparar estructura tmp models_augmented and models
    base = tmp_path / 'ml'
    ma = base / 'models_augmented'
    m = base / 'models'
    tenant_dir = ma / 'tenantA'
    global_dir = ma / 'global'
    # Crear tenant-specific model
    make_dummy_model_and_vectorizer(tenant_dir)
    # Crear global augmented model
    make_dummy_model_and_vectorizer(global_dir)

    # Patchar los paths en los módulos predict and explain
    monkeypatch.setattr(predict_module, 'MODELS_AUG_DIR', ma)
    monkeypatch.setattr(predict_module, 'MODELS_DIR', m)
    monkeypatch.setattr(explain_module, 'MODELS_AUG_DIR', ma)
    monkeypatch.setattr(explain_module, 'MODELS_DIR', m)

    # Ahora probar predict_text con tenantA (debe usar tenant dir)
    res_tenant = predict_module.predict_text('ejemplo de riesgo', tenant_id='tenantA')
    assert '_model_path' in res_tenant
    assert str(tenant_dir) in res_tenant['_model_path']
    assert res_tenant['label'] in ['Bajo','Medio','Alto']

    # Probar predict_text con tenant inexistente -> fallback a global
    res_fallback = predict_module.predict_text('ejemplo de riesgo', tenant_id='unknownTenant')
    assert '_model_path' in res_fallback
    assert str(global_dir) in res_fallback['_model_path']

    # Test explain also funciona y muestra _model_path
    ex = explain_module.explain_text('ejemplo de riesgo', top_k=3, tenant_id='tenantA')
    assert '_model_path' in ex
    assert ex['predicted_class'] in ['Bajo','Medio','Alto']


if __name__ == '__main__':
    pytest.main([__file__])

