# Etapa de build
FROM node:20-bookworm AS builder

WORKDIR /app

# Copiamos sólo los manifiestos primero para cachear instalaciones
COPY package*.json ./

# Instalar dependencias (usa npm ci si existe package-lock.json; si no, npm install)
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

# Construir la app
RUN npm run build

# Etapa de runtime (producción)
FROM node:20-bookworm-slim AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
# Puedes cambiar el puerto en tiempo de ejecución con -e PORT=XXXX
ENV PORT=3000

# Copiamos sólo lo necesario para ejecutar next start
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
# Copiamos node_modules construidos en la etapa anterior para evitar reinstalar
COPY --from=builder /app/node_modules ./node_modules

# Exponer el puerto por defecto de Next.js
EXPOSE 3000

# Comando de arranque: Next.js en modo producción
CMD ["npm", "run", "start"]
