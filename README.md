# Proyecto IyE – Plataforma de cumplimiento con IA

Repositorio monorepo con:
- Backend Spring Boot 3.5 (Java 21) para gestión de empresas, riesgos, regulaciones y auditorías asistidas por IA. Incluye módulos de ML (clasificación, NER, generación).
- Frontend Next.js 16 (React 19, TypeScript) para la interfaz de cumplimiento, dashboards y flujo de auditorías.

## Estructura
- [BackendIE](BackendIE/README.md): API REST, modelos ML, scripts de demo y orquestación docker-compose.
- [Frontend_2](Frontend_2/README.md): interfaz web, componentes UI y configuración de Next.js.

## Requisitos
- Java 21 y Maven (o `./mvnw`).
- Node.js 20+ (recomendado) y pnpm o npm.
- Opcional: puedes levantar servicios externos (DB, observabilidad, Ollama) con Docker Compose más adelante, pero hoy el backend está previsto para ejecución local.

## Puesta en marcha rápida
### Backend (desarrollo)
1) Entrar a `BackendIE` y crear `.env` (o `application.properties`) con credenciales mínimas, ej.:
```
DB_POSTGRES_HOST=localhost
DB_POSTGRES_PORT=5432
DB_POSTGRES_NAME=postgres
DB_POSTGRES_USER=postgres
DB_PASSWORD=postgres
TOKEN_SECRET=changeme
OLLAMA_BASE_URL=http://localhost:11434
```
2) Ejecutar:
```
./mvnw spring-boot:run
```
3) Swagger UI: http://localhost:8080/swagger-ui/index.html

### Frontend (desarrollo)
1) Entrar a `Frontend_2` y crear `.env.local`:
```
NEXT_PUBLIC_API_URL=http://localhost:8080
```
2) Instalar dependencias (elige gestor):
```
pnpm install
# o
npm install
```
3) Levantar dev server (usa puerto 3001 por defecto):
```
pnpm dev
```
4) Navegar a http://localhost:3001

## Stack con Docker Compose (opcional, no habilitado en esta entrega)
La orquestación docker-compose está preparada pero no se usó en esta entrega. Si en el futuro deseas contenedores para Postgres, Mongo, Ollama u observabilidad, revisa [BackendIE/docker-compose.yml](BackendIE/docker-compose.yml) y ajusta variables antes de levantar el stack.

## Pruebas y calidad
- Backend: `./mvnw test`
- Frontend: `pnpm lint` (no hay pruebas definidas en package.json a la fecha).

## Documentación útil
- Guía backend y endpoints: [BackendIE/README.md](BackendIE/README.md) y scripts en [BackendIE/scripts](BackendIE/scripts).
- Guía frontend y recomendaciones de entorno: [Frontend_2/README.md](Frontend_2/README.md).
- Notas y guiones de demo: [BackendIE/docs](BackendIE/docs).

## Estado rápido
Checklist de funcionamiento esperado para revisión:
- Backend responde en http://localhost:8080/actuator/health
- Swagger UI accesible en /swagger-ui/index.html
- Frontend carga en http://localhost:3001 consumiendo el backend configurado
- Docker compose levanta sin errores de healthcheck