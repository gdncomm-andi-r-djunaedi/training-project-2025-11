# Multi-stage Dockerfile for Marketplace Microservices
# Usage: docker build --build-arg SERVICE_NAME=<service> -t marketplace/<service>:latest .
# Services: api-gateway, member, product, cart, data-seeder

# =============================================================================
# Stage 1: Build stage
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy parent pom and module poms first for dependency caching
COPY pom.xml ./
COPY common-utils/pom.xml ./common-utils/
COPY api-gateway/pom.xml ./api-gateway/
COPY member/pom.xml ./member/
COPY product/pom.xml ./product/
COPY cart/pom.xml ./cart/
COPY data-seeder/pom.xml ./data-seeder/

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY common-utils/src ./common-utils/src
COPY api-gateway/src ./api-gateway/src
COPY member/src ./member/src
COPY product/src ./product/src
COPY cart/src ./cart/src
COPY data-seeder/src ./data-seeder/src

# Build argument for selecting which service to build
ARG SERVICE_NAME

# Install parent POM and common-utils to local Maven repository
# -am ensures parent POM is also built/installed
RUN mvn clean install -N -DskipTests -B && \
    mvn clean install -pl common-utils -am -DskipTests -B

# Build and repackage the service module (creates executable fat JAR)
# If building data-seeder, we need to install member and product modules first (as plain JARs)
RUN if [ "$SERVICE_NAME" = "data-seeder" ]; then \
      mvn install -pl member,product -DskipTests -Dspring-boot.repackage.skip=true -B; \
    fi && \
    mvn package spring-boot:repackage -pl ${SERVICE_NAME} -DskipTests -B

# =============================================================================
# Stage 2: Runtime stage
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Add non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Build argument for selecting which service to run
ARG SERVICE_NAME

# Copy the built jar from builder stage
COPY --from=builder /app/${SERVICE_NAME}/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

# Expose port (default 8080, can be overridden)
EXPOSE ${SERVER_PORT:-8080}

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
