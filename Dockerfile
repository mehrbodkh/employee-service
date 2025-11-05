# ---- BUILD STAGE ----
FROM gradle:9.1.0-jdk24 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon -x test

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app

# Copy compiled classes and dependencies
COPY --from=builder /app/server/build/libs/*-all.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]