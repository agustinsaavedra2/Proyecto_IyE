# BackendIE - Dev infra

Este README contiene instrucciones rápidas para levantar los servicios de desarrollo e inspeccionar observabilidad.

Requisitos:
- Docker & Docker Compose
- Variables de entorno en .env (DB_PASSWORD, TOKEN_SECRET, etc.)

Levantar servicios:

1) Crear archivo `.env` con las variables necesarias:
   - DB_PASSWORD= Contraseña de base de datos postgres
   - TOKEN_SECRET= String de largo 40 bytes, se puede generar con: 
```powershell
powershell -Command "$random = -join ((65..90) + (97..122) + (48..57) | Get-Random -Count 40 | ForEach-Object {[char]$_}); [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($random))"
```
   - FRONTEND_URL=http://localhost:3001
   - EMAIL_USERNAME=Correo
   - EMAIL_FROM=Correo
   - EMAIL_PASSWORD= App_Password (privada)

2) Levantar servicios:
```
docker compose up --build
```

Servicios expuestos:
- Backend: http://localhost:8080
- Ollama: http://localhost:11434 (si se ejecuta en compose)
- Prometheus: http://localhost:9090
- Jaeger UI: http://localhost:16686
- Nginx (reverse proxy): http://localhost

Actuator:
- /actuator/health
- /actuator/prometheus

Notas:
- Imagen de Ollama en Docker puede no estar públicamente disponible o requerir pasos adicionales. Si ya tienes Ollama local, ajusta `OLLAMA_BASE_URL` en tu `.env` para que apunte a http://host.docker.internal:11434 o a la URL local.
- Para ambientes productivos usar Redis-backed Bucket4j para rate-limit y no la implementación en memoria.

