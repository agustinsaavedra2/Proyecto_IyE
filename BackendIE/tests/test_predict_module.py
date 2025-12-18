import pytest
from ml import predict as predict_module


def test_predict_smoke():
    # Este test asume que existe ml/models_augmented o ml/models con artifacts válidos
    try:
        res = predict_module.predict_text('empresa con multa por incumplimiento')
    except FileNotFoundError:
        pytest.skip('Model artifacts not found; skipping')
    assert isinstance(res, dict)
    assert 'label' in res and 'probabilities' in res
    assert sum(res['probabilities']) > 0.0

