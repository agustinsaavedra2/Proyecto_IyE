Fine-tuning LLM (carpeta de referencia)

Propósito:
- Indicar pasos mínimos y formato para crear un dataset y entrenar/afinar un LLM local en Ollama (o similar). Esto NO modifica el código de producción; es un pipeline documentado para seguir.

Requerimientos:
- Ollama corriendo localmente en http://localhost:11434
- Acceso a los PDFs y a los textos anotados (en `ml/dataset/` y `ml/data/`)

Formato de datos sugerido (ejemplos para fine-tuning basada en prompt-completion):
- TSV/JSONL con campos: {"prompt": "<contexto y pregunta>", "completion": "<texto esperado>"}
- Ejemplo: {"prompt":"Genera una política de higiene para restaurante X. Contexto: <texto de ley>...","completion":"::POLITICA::\n::titulo:: Politica higiene \n::contenido:: ... ::END_POLITICA::"}

Pasos rápidos (alto nivel):
1) Extraer textos relevantes desde PDFs (usar los scripts ya en `ml/` para extraer y limpiar si existen; o usar PyPDF2/pdfminer).
2) Anotar o construir pares prompt-completion (puedes usar plantillas y ejemplos humanos para few-shot o construir dataset grande para fine-tuning).
3) Usar la API de Ollama o su comando CLI para subir/entrenar el modelo (dependiendo de la versión de Ollama). Por ejemplo (ejemplo hipotético):
   ollama train --model base-model --data ./llm_finetune/dataset.jsonl --output fine_tuned_model
4) Cargar el modelo entrenado en Ollama y actualizar `spring.ai.ollama.chat.model` en `application.properties` para usar el nuevo modelo.

Notas:
- Si tu versión de Ollama no soporta 'train' vía CLI, consulta la documentación local de Ollama o usa otra herramienta de fine-tune (Alpaca, llama.cpp adaptaciones).
- Para la entrega mínima, el equipo puede usar prompt-engineering y few-shot en lugar de un fine-tune pesado.

Puedo preparar scripts que conviertan PDFs a JSONL de prompts si confirmas.

