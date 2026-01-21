import pytest
import subprocess
from pathlib import Path


def test_trainer_creates_artifacts(tmp_path):
    # Ejecuta trainer con un pequeño index temporal
    idx = tmp_path / 'index.jsonl'
    idx.write_text('{"text":"ejemplo uno","label":"Bajo"}\n{"text":"ejemplo dos","label":"Alto"}\n')
    outdir = tmp_path / 'models'
    cmd = ['python', 'ml/trainer.py', '--index', str(idx), '--out-dir', str(outdir), '--label-method', 'from_file', '--model', 'logreg']
    res = subprocess.run(cmd, capture_output=True, text=True)
    assert res.returncode == 0
    assert (outdir / 'model.joblib').exists()
    assert (outdir / 'vectorizer.joblib').exists()
    assert (outdir / 'manifest.json').exists()

