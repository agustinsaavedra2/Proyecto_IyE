Demo guion (5 minutos) - BackendIE

Objetivo: demostrar en vivo el backend y la integración ML mínima usando la colección de Postman y/o el script demo_local.ps1.

Preparación (antes de la demo)
- Arrancar la base de datos Postgres y Mongo locales si aplica (en .env está configurado para localhost).
- Ejecutar la app backend: mvn spring-boot:run (o java -jar target/BackendIE-0.0.1-SNAPSHOT.jar).
- Abrir Postman y cargar `scripts/postman_collection.json` (Import -> File).
- O alternativamente abrir PowerShell y tener `scripts/demo_local.ps1` listo.

Estructura del guion (5 minutos)
- 00:00-00:30 — Introducción rápida (30s)
  - Contexto: automatización de auditorías para MiPymes/restaurantes.
  - Qué se va a mostrar: endpoints CRUD, multitenancy, ML (generación doc / auditoría con Ollama).

- 00:30-01:30 — Autenticación y creación de entidad principal (1 min)
  - Ejecutar request: `Register Admin` (si ya existe, OK).
  - Ejecutar `Login - get token`. Copiar token a variable `{{token}}` de la colección.
  - Ejecutar `Create Empresa` (POST /api/empresas/registrar) — demuestra validaciones.

- 01:30-02:30 — Lectura con filtros y paginación (1 min)
  - Ejecutar `List Empresas (DTO) - no tenant header` — muestra todas.
  - Ejecutar `List Empresas (DTO) - with X-Categoria-id` — demuestra aislamiento multitenant.
  - Explicar que el tenant se resuelve por `X-Categoria-id` o por usuario autenticado (TenantContext).

- 02:30-03:30 — CRUD de Riesgos (1 min)
  - Ejecutar `Create Riesgo` (rellenar `{{empresaId}}`).
  - Ejecutar `Update Riesgo` para cambiar nivel.
  - Ejecutar `Delete Riesgo`.
  - Mostrar manejo de errores si se intenta actualizar sin permisos o con campos inválidos.

- 03:30-04:30 — ML / Ollama (1 min)
  - Ejecutar `Create Auditoria (Ollama)` (simula llamada a Ollama local). Mostrar la respuesta.
  - Ejecutar `Generar Documento LLM (example)` para generar política/plantilla. Explicar que el modelo fine‑tuned se usaría aquí (OLLAMA local cargado).

- 04:30-05:00 — Cierre (30s)
  - Resumen de lo demostrado: endpoints, multitenant, ML integrado.
  - Mencionar entregables ML restantes: clasificación de riesgos, NER, generación fine‑tuned (se puede demostrar con los scripts en `ml_training/` y `llm_finetune/`).
  - Preguntas.

Consejos técnicos rápidos
- Para grabar: usa OBS o la grabadora de pantalla; ejecuta cada request desde Postman y muestra la respuesta JSON.
- Para pruebas automatizadas en vez de Postman: usar Newman (npm) con la colección: `newman run scripts/postman_collection.json --env-var "token=..."`.

Archivos relevantes
- `scripts/postman_collection.json` — colección Postman exportable.
- `scripts/demo_local.ps1` — script PowerShell para demo automatizada.
- `src/main/java/com/backendie/...` — código backend principal.

Si quieres, genero también:
- Un archivo `.env.sample` con las variables necesarias para correr localmente.
- Un script `scripts/run_demo_newman.ps1` que ejecute Newman automáticamente usando la colección (necesario instalar newman).

Dime si quieres que genere: `run_demo_newman.ps1` y un `.env.sample` ahora.
