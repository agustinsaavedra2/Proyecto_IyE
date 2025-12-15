def test_predict_importable():
    # Smoke test: ensure module imports and wrapper exists
    import ml.predict as pred
    assert hasattr(pred, 'predict_text')

