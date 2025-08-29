# syntax=docker/dockerfile:1

### Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
# 產生可執行的 Spring Boot Fat Jar
RUN mvn -q -DskipTests package spring-boot:repackage
# 只挑不是 .original 的 jar，固定成 /app/app.jar
RUN set -eux; JAR="$(ls target/*.jar | grep -v '\.original$' | head -n1)"; \
    cp "$JAR" /app/app.jar

### Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
