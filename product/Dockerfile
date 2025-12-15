# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper files and pom.xml to download dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Expose the port your Spring Boot application listens on (default is 8080)
EXPOSE 8080

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]