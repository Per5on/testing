# Build the Spring Boot application in a reproducible JDK 17 environment.
FROM gradle:8.11.1-jdk17 AS build
WORKDIR /workspace
COPY . .
RUN gradle bootJar --no-daemon

# Small non-root runtime image.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd --system --uid 10001 --create-home appuser
COPY --from=build /workspace/build/libs/bg-prices-backend-0.1.0.jar /app/app.jar
USER 10001:10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
