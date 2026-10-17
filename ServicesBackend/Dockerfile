# Java 21 + Gradle Wrapper del proyecto; sin Gradle preinstalado.
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test && \
    cp build/libs/*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-jammy AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    useradd -r -u 10001 -d /app -s /usr/sbin/nologin appuser
WORKDIR /app
COPY --from=build --chown=appuser:appuser /workspace/app.jar /app/app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
