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
RUN mkdir -p api-gateway/src/main/java member/src/main/java cart/src/main/java

# Copy source code for modules we need
COPY common/src common/src
COPY product/src product/src

# Build the application
RUN mvn clean package -pl common,product -am -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/product/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
