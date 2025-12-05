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
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user
RUN groupadd -r appuser && \
    useradd -r -g appuser appuser && \
    chown -R appuser:appuser /app

# Expose ports
EXPOSE 9096 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+UseG1GC"

# Run as non-root
USER appuser

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]