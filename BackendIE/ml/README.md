ML dentro de BackendIE

Instrucciones rápidas

1) Instalar dependencias (virtualenv recomendado):
   cd ml/service; pip install -r requirements.txt

2) Entrenar (si tienes `ml/index.jsonl`):
   python ../trainer.py --index ../index.jsonl --out-dir ../models

3) Ejecutar servicio de predicción (espera encontrar `ml/models/model.joblib` y `vectorizer.joblib`):
   cd ml/service
   uvicorn app:app --host 0.0.0.0 --port 8000

4) Predicción (ejemplo):
   curl -X POST 'http://localhost:8000/predict' -H 'Content-Type: application/json' -d '{"text":"empresa con multa reciente por incumplimiento"}'

Formato `ml/index.jsonl` esperado:
Cada línea debe ser un JSON con al menos las keys:
- id: identificador
- text: texto del chunk
- meta: objeto con metadata, por ejemplo {"source":"archivo.pdf"}
- label: (opcional) 'Bajo'|'Medio'|'Alto'

Ejemplo de línea:
{"id":"doc1_chunk01","text":"...","meta":{"source":"dataset/doc.pdf"},"label":"Alto"}

