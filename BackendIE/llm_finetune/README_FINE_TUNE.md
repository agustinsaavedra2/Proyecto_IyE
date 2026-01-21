Fine-tuning con Ollama (llama3.1:8b) - Guía rápida

Precondiciones:
- Ollama corriendo localmente en http://localhost:11434
- Versión objetivo del modelo: "llama3.1:8b" (asegúrate de tenerlo descargado o disponible en Ollama)

Flujo propuesto:
1) Preparar dataset de training:
   - Ejecuta: python llm_finetune/pdfs_to_jsonl.py --dataset-dir ml/dataset --out llm_finetune/dataset.jsonl
   - Esto generará ejemplos prompt/completion (necesitan revisión humana y ampliación para calidad).

2) Convertir a formato que Ollama acepte (si aplica) y lanzar entrenamiento.
   - Consultar si tu versión de Ollama soporta 'train' o 'pull' con fine-tune. Si tu Ollama soporta CLI de entrenamiento, el comando típico sería (hipotético):
     ollama train --model llama3.1:8b --data llm_finetune/dataset.jsonl --output fine_tuned_llama3_8b
   - Si tu Ollama NO soporta train, usar alternativa: crear prompts few-shot y usar prompt-engineering en `OllamaResponseService`.

3) Cargar modelo en Ollama y actualizar backend:
   - Cargar modelo fine-tuned con la API/CLI de Ollama.
   - Actualizar propiedad `spring.ai.ollama.chat.model` o la configuración que uses para apuntar al nuevo modelo.

Notas y recomendaciones:
- La calidad del fine-tune depende de la calidad y tamaño del dataset. Revisar ejemplos generados por `pdfs_to_jsonl.py` antes de entrenar.
- Para la demo, el fine-tune NO es estrictamente necesario si usas prompt-engineering con few-shot y la integración del modelo clásico como contexto (ya implementada).
- Puedo intentar ejecutar comandos de entrenamiento si confirmas que tu Ollama soporta `train`. Para comprobarlo, puedes ejecutar localmente:
  curl -s http://localhost:11434/api/tags  
  # y revisar la documentación de Ollama local o correr: ollama --help


