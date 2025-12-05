###########################################
# 1. BUILD STAGE (Maven + JDK 21)
###########################################
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy POM early to leverage Docker cache
COPY pom.xml .

###############################################
# 1️⃣ Copy common-proto jar to Docker
###############################################
COPY libs/common-proto-0.0.1-SNAPSHOT.jar /tmp/common-proto.jar

# 1.1 Validate file exists (prevent silent failures)
RUN if [ ! -f /tmp/common-proto.jar ]; then \
    echo "❌ ERROR: common-proto.jar not found in /tmp"; \
    ls -lah /tmp; \
    exit 1; \
    else \
    echo "✅ common-proto.jar found:"; \
    ls -lah /tmp/common-proto.jar; \
    fi

###############################################
# 2️⃣ Install jar into local Maven repo
###############################################
RUN mvn install:install-file \
    -Dfile=/tmp/common-proto.jar \
    -DgroupId=com.gdn.training \
    -DartifactId=common-proto \
    -Dversion=0.0.1-SNAPSHOT \
    -Dpackaging=jar

###############################################
# 3️⃣ Copy project source
###############################################
COPY src ./src

###############################################
# 4️⃣ Build project
###############################################
RUN mvn clean package -DskipTests

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