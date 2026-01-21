# BackendIE - Docker Compose (local development)

This README explains how to run the full stack locally with Docker Compose (Postgres, Mongo, Ollama, Backend, Nginx, Prometheus, Jaeger, EFK).

Prerequisites:
- Docker and docker-compose installed locally.
- Ollama image used here (`ollama/ollama:latest`). If you already run Ollama on the host, you can remove the `ollama` service and set `OLLAMA_BASE_URL` to the host address.

Quick start (Linux/Windows PowerShell):

1) Build and start the stack (detached):

```powershell
docker compose up --build -d
```

2) Check logs for backend:

```powershell
docker compose logs -f backend
```

3) Validate services:
- Backend: http://localhost:8080/actuator/health
- Prometheus: http://localhost:9090
- Jaeger UI: http://localhost:16686
- Kibana: http://localhost:5601
- Ollama: http://localhost:11434/v1/models

Notes:
- The backend image runs with the OpenTelemetry Java agent if available and the OTEL env vars point to the Jaeger collector.
- Database seeding occurs on application startup via `DatabaseSeeder`.
- To shut down and remove volumes (data), run:

```powershell
docker compose down -v
```

If you'd like I can try to start the compose stack here and validate endpoints (requires Docker available in this environment).
