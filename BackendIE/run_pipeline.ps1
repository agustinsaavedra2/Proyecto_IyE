# Run minimal pipeline: create venv, install deps, evaluate existing model and optionally run service
param(
    [switch]$RunService
)

python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r .\requirements.txt

# Evaluate augmented model if present, otherwise evaluate models
if (Test-Path .\ml\models_augmented\manifest.json) {
    Write-Host "Evaluando ml/models_augmented"
    python .\ml\eval.py --index .\ml\index.jsonl --model-dir .\ml\models_augmented
} elseif (Test-Path .\ml\models\manifest.json) {
    Write-Host "Evaluando ml/models"
    python .\ml\eval.py --index .\ml\index.jsonl --model-dir .\ml\models
} else {
    Write-Host "No hay manifest.json en modelos. Ejecuta trainer.py si necesitas entrenar."
}

if ($RunService) {
    Write-Host "Iniciando servicio ML (uvicorn) en ml/service"
    Push-Location .\ml\service
    uvicorn app:app --host 0.0.0.0 --port 8000
    Pop-Location
}

