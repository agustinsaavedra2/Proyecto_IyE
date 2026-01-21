# 1. Imagen base ligera de Python
FROM python:3.9-slim

# 2. Variables de entorno para optimizar Python en Docker
# Evita archivos .pyc y asegura que los logs salgan en tiempo real
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# 3. Directorio de trabajo dentro del contenedor
WORKDIR /app

# 4. Copiar dependencias e instalarlas
# Asegúrate de haber renombrado 'requeriments.txt' a 'requirements.txt'
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. Copiar el archivo principal de la aplicación
COPY app.py .

# 6. CRUCIAL: Copiar la carpeta 'ml' completa
# Esto coloca la carpeta 'ml' al lado de 'app.py' dentro del contenedor
# para que 'from ml import predict' funcione igual que en tu PC.
COPY ml/ ./ml/

# 7. Crear carpeta opcional por si el código la busca
RUN mkdir -p ml/models_augmented

# 8. Exponer el puerto de FastAPI
EXPOSE 8000

# 9. Comando de arranque
# Ejecuta uvicorn buscando la app dentro de app.py
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]