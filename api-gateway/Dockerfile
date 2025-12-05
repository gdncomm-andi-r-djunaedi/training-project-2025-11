FROM eclipse-temurin:21-jdk-alpine as builder

# Install Maven
RUN apk add --no-cache maven

WORKDIR /app

# Copy pom files for all modules (required by parent pom)
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY member/pom.xml member/pom.xml
COPY product/pom.xml product/pom.xml
COPY cart/pom.xml cart/pom.xml

# Create empty src directories for modules we don't build
RUN mkdir -p member/src/main/java product/src/main/java cart/src/main/java

# Copy source code for modules we need
COPY common/src common/src
COPY api-gateway/src api-gateway/src

# Build the application
RUN mvn clean package -pl common,api-gateway -am -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
