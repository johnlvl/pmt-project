# Multi-stage Dockerfile for Spring Boot backend (Java 21)

# 1) Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Pre-copy pom to leverage Docker layer caching
COPY pom.xml ./
RUN mvn -q -e -B -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -q -e -B -DskipTests package

# 2) Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Allow optional JVM opts (e.g., -Xms256m -Xmx512m)
ENV JAVA_OPTS=""

# Copy fat jar from build stage (use wildcard to match version)
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

# Pass Spring config via environment variables at runtime as needed:
#   SPRING_DATASOURCE_URL
#   SPRING_DATASOURCE_USERNAME
#   SPRING_DATASOURCE_PASSWORD

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
