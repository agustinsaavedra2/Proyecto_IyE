# --- Etapa 1: Build (Construcción) ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar la caché de capas de Docker
COPY pom.xml .
# Descargamos dependencias (esto tardará la primera vez, luego es instantáneo)
RUN mvn dependency:go-offline

# Copiamos el código fuente
COPY src ./src

# Compilamos y empaquetamos (saltando tests para ir rápido en despliegue)
RUN mvn clean package -DskipTests

# --- Etapa 2: Runtime (Ejecución) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto
EXPOSE 8080

# Variables de entorno por defecto (se sobreescriben en docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

# Ejecutamos la app
ENTRYPOINT ["java", "-jar", "app.jar"]