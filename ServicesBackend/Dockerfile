# Etapa 1: compila con el Gradle Wrapper del proyecto (no requiere Gradle instalado)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
# Por si el checkout llegó con CRLF (Windows) y sin permiso de ejecución
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew --no-daemon --version >/dev/null
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test && cp build/libs/*.jar /workspace/app.jar

# Etapa 2: solo el JRE y el JAR, con usuario sin privilegios
FROM eclipse-temurin:21-jre-jammy AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    useradd -r -u 10001 -d /app -s /usr/sbin/nologin appuser
WORKDIR /app
COPY --from=build --chown=appuser:appuser /workspace/app.jar /app/app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
  CMD curl --fail --silent "http://127.0.0.1:${PORT:-8080}/actuator/health" || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+UseSerialGC", "-jar", "/app/app.jar"]
