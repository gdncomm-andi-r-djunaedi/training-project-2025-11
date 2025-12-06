# ================================
# 1. Build Stage
# ================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src

RUN mvn -q -e -DskipTests clean package

# ================================
# 2. Run Stage
# ================================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
