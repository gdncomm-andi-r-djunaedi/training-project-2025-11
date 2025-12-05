###########################################
# 1. BUILD STAGE (Maven + JDK 21)
###########################################
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# copy pom + source
COPY pom.xml .
COPY src ./src

# COPY local dependency (common-proto)
COPY libs ./libs

# Build project
RUN mvn clean package -DskipTests


###########################################
# 2. RUNTIME STAGE (Slim JRE 21)
###########################################
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Install wget and grpc_health_probe
RUN apt-get update && \
    apt-get install -y wget && \
    wget -qO /bin/grpc_health_probe \
    https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/v0.4.24/grpc_health_probe-linux-amd64 && \
    chmod +x /bin/grpc_health_probe && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && \
    useradd -r -g appuser appuser && \
    chown -R appuser:appuser /app

# Expose ports
EXPOSE 9096 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+UseG1GC"

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD ["/bin/grpc_health_probe", "-addr=:9096"]

# Run as non-root
USER appuser

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]