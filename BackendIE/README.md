# BackendIE - MVP entrega 1

Resumen rápido:
- Backend Spring Boot para auditorías de cumplimiento.
- Endpoints REST para gestión de empresas, políticas, protocolos, procedimientos, auditorías y ML (clasificación/NER/generación).

Cómo ejecutar localmente (mínimo para demo):
1. Java 21 y Maven instalados.
2. Configurar `.env` o `application.properties` con conexión a DB (puedes usar H2 o Postgres).
3. Levantar la aplicación:

```bash
./mvnw spring-boot:run
```

4. Swagger UI: http://localhost:8080/swagger-ui/index.html

Script de demostración (curl) disponible en `scripts/demo.ps1`.

Endpoints clave para demo:
- POST /api/empresas/registrar — registra una empresa
- GET /api/empresas/resumen — lista empresas
- CRUD /api/riesgos — crear/leer/actualizar/eliminar riesgos
- POST /api/ollama/crearAuditoria — generar auditoría (usa Ollama local)
- POST /api/ml/upload — subir documento ML
- POST /api/ml/predict — ejecutar predicción de riesgo

Testing:
- `mvn test` ejecuta tests unitarios y de integración (H2/Flapdoodle)

Demo en 5 minutos (sugerencia): usar `scripts/demo.ps1` para ejecutar flujo CRUD y ML.

