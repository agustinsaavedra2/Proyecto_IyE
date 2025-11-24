# Multi-stage Dockerfile for Spring Boot application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Copy sources
COPY src ./src

# Make mvnw executable
RUN chmod +x mvnw
# Build jar
RUN ./mvnw -DskipTests clean package -U

# Download OpenTelemetry Javaagent for tracing (copied into final image)
ARG OTEL_AGENT_VERSION=1.28.0
RUN wget -q -O opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar || true

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# copy agent if available
COPY --from=build /app/opentelemetry-javaagent.jar /opt/opentelemetry/opentelemetry-javaagent.jar

EXPOSE 8080

# Use the OTEL agent if present; the agent will be a no-op if env vars not set.
ENTRYPOINT ["sh","-c","if [ -f /opt/opentelemetry/opentelemetry-javaagent.jar ]; then exec java -javaagent:/opt/opentelemetry/opentelemetry-javaagent.jar -jar /app/app.jar; else exec java -jar /app/app.jar; fi"]