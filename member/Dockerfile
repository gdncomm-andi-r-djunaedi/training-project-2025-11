###########################################
# 1. BUILD STAGE (Maven + JDK 21)
###########################################
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# ------------------------------------------------
# Copy pom.xml first for dependency caching
# ------------------------------------------------
COPY pom.xml .

# Download dependencies (can be cached if pom.xml unchanged)
RUN mvn -DskipTests dependency:go-offline

# ------------------------------------------------
# Copy full project and build JAR
# ------------------------------------------------
COPY src ./src

RUN mvn -DskipTests clean package

# JAR name argument (in case version changes)
ARG JAR_FILE=target/member-0.0.1-SNAPSHOT.jar


###########################################
# 2. RUNTIME STAGE (Slim JRE 21)
###########################################
FROM eclipse-temurin:21-jre
WORKDIR /app

# ------------------------------------------------
# Copy ANY jar generated
# ------------------------------------------------
COPY --from=builder /app/target/*.jar app.jar

# ------------------------------------------------
# Allow Hibernate temp usage (best practice)
# ------------------------------------------------
VOLUME /tmp

# ------------------------------------------------
# Expose ports
# - gRPC default port
# - Spring Boot / actuator port
# ------------------------------------------------
EXPOSE 9095
EXPOSE 8081

# ------------------------------------------------
# Environment variables for runtime
# ------------------------------------------------

# Overrideable by docker run / compose
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_URL=""
ENV DB_USER=""
ENV DB_PASSWORD=""
ENV REDIS_HOST=""
ENV REDIS_PORT=6379

# JVM container optimization
ENV JAVA_OPTS="\
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75 \
    -XX:+UseG1GC \
    "

# ------------------------------------------------
# HEALTHCHECK (optional but recommended)
# Will attempt to check actuator if enabled
# ------------------------------------------------
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# ------------------------------------------------
# ENTRY POINT
# ------------------------------------------------
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
