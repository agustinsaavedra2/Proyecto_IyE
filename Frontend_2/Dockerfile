# Etapa de build
FROM node:20-bookworm AS builder

WORKDIR /app

# Copiamos sólo los manifiestos primero para cachear instalaciones
COPY package*.json ./

# Instalar dependencias
RUN if [ -f package-lock.json ]; then \
      npm ci --no-audit --no-fund; \
    else \
      npm install --no-audit --no-fund; \
    fi

# Copiar el resto del código del frontend
COPY . .

# Desactivar la telemetría de Next durante el build
ENV NEXT_TELEMETRY_DISABLED=1
ENV NODE_ENV=production

# --- AQUÍ ESTÁ EL ARREGLO ---
# 1. Recibimos el argumento desde docker-compose
ARG NEXT_PUBLIC_API_URL
# 2. Lo convertimos en variable de entorno para que el build lo vea
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
# -----------------------------

# Construir la app (Ahora sí "quemará" la URL correcta en el JS)
RUN npm run build

# Etapa de runtime (producción)
FROM node:20-bookworm-slim AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000

# Copiamos sólo lo necesario para ejecutar next start
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/node_modules ./node_modules

EXPOSE 3000

CMD ["npm", "run", "start"]