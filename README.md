# 🚀 Proyecto IyE — Despliegue mínimo con Docker

[![Docker](https://img.shields.io/badge/Docker-ready-blue)](https://www.docker.com/) [![Java 21](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/) [![Next.js](https://img.shields.io/badge/Next.js-16-black)](https://nextjs.org/)

Descripción
-----------
Proyecto IyE es una plataforma de cumplimiento y auditoría asistida por IA. Permite modelar empresas, gestionar riesgos y regulaciones, ejecutar auditorías asistidas por ML (clasificación, extracción) y ofrecer una interfaz web para generar, seguir y resolver hallazgos de cumplimiento.

Tabla de contenidos
------------------
- [Tecnologías](#tecnologías)
- [Rápido: despliegue con Docker](#rápido-despliegue-con-docker)
- [Comprobaciones](#comprobaciones)
- [Consejos y resolución de problemas](#consejos-y-resolución-de-problemas)
- [Documentación adicional](#documentación-adicional)

Tecnologías
-----------
- Backend: Java 21, Spring Boot 3.5, Maven. Usa PostgreSQL y MongoDB; integra Ollama y un microservicio ML en Python.
- Frontend: Next.js 16 (React 19) con TypeScript. Node.js 20+ (pnpm/npm).

Rápido: despliegue con Docker
----------------------------
Requisitos: Docker Engine y el plugin `docker compose`.

El `docker-compose.yml` principal está en la raíz del repositorio: [docker-compose.yml](docker-compose.yml).

Pasos (desde la raíz del repo):

```bash
# Construir y levantar el stack
docker compose up -d --build

# Ver servicios activos
docker compose ps

# Ver logs del backend
docker compose logs -f backend

# Parar y eliminar
docker compose down
```

Puertos por defecto
- Backend: `8080`
- Frontend: `3000` (http://localhost:3000)

Comprobaciones
--------------
- Espera a que todos los servicios estén en estado "healthy" (`docker compose ps`).
- Si el backend no arranca, inspecciona `docker compose logs -f backend`.

Consejos y resolución de problemas
----------------------------------
- Windows: si usas Docker Desktop, asigna suficiente memoria/CPU.
- Puertos: modifica `docker-compose.yml` si necesitas otros puertos.
- Base de datos externa: actualiza la URL en las variables de entorno del servicio `backend` en `docker-compose.yml`.

Documentación adicional
----------------------
- Backend: [BackendIE](BackendIE/README.md)
- Frontend: [Frontend_2](Frontend_2/README.md)

