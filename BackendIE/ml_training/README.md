Entrenamiento - Clasificador de Riesgos (carpeta de referencia)

Propósito:
- Documentar dónde están los scripts existentes para entrenar el clasificador de riesgos y cómo usarlos sin modificarlos.

Archivos relevantes ya en el repo (no modificar):
- `ml/trainer.py` : script principal para entrenar TF-IDF + LogisticRegression o XGBoost. Usa `ml/index.jsonl` para leer textos y etiquetas.
- `ml/eval.py` : evalúa un modelo guardado (calcula accuracy y f1_macro) sobre un `index.jsonl` etiquetado.
- `ml/predict.py` : script de inferencia que carga `ml/models/model.joblib` y `ml/models/vectorizer.joblib`.

Cómo entrenar (ejemplo local):
1) Preparar `ml/index.jsonl` con registros JSON por línea: {"text": "...", "label": "Bajo|Medio|Alto"}
2) Ejecutar (entorno Python con dependencias instaladas):
   python ml/trainer.py --index ml/index.jsonl --out-dir ml/models --label-method from_file --model logreg
3) Validar con eval:
   python ml/eval.py --index ml/index.jsonl --model-dir ml/models

Notas:
- NO modificar estos scripts en esta fase. Cualquier experimento nuevo debe colocarse en otra carpeta (ej. `ml_experiments/`) para no interferir con la entrega.
- Los artefactos generados se guardan en `ml/models` (o `ml/models_augmented` si se generan mejoras). El backend Java busca `ml/models` por defecto.

"Try it" rápido (PowerShell):
  python .\ml\trainer.py --index .\ml\index.jsonl --out-dir .\ml\models --label-method from_file --model logreg
  python .\ml\eval.py --index .\ml\index.jsonl --model-dir .\ml\models

Si necesitas que genere scripts de experimentos reproducibles, lo hago en una carpeta separada al confirmar.
