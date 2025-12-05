# syntax=docker/dockerfile:1
# ================================
# Shared Builder - Maven + JDK 21
# Builds all modules and collects JARs
# NOTE: No cache mount to ensure fresh dependencies on each build
# ================================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy everything
COPY . .

# Clean all target directories first to ensure fresh build
RUN echo "═══════════════════════════════════════════" && \
    echo "  🧹 Cleaning target directories..." && \
    echo "═══════════════════════════════════════════" && \
    rm -rf */target grpc-contract/target && \
    echo "✅ Target directories cleaned!"

# Build all modules with Maven (parallel -T 4C)
# No --mount=type=cache to avoid stale dependencies
RUN echo "═══════════════════════════════════════════" && \
    echo "  🔨 Building with Maven (-T 4C parallel)..." && \
    echo "═══════════════════════════════════════════" && \
    mvn clean install -DskipTests -T 4C && \
    echo "✅ Maven build completed!"

# Collect JARs
RUN mkdir -p /app/jars && \
    cp member/target/member.jar /app/jars/ && \
    cp catalog/target/catalog.jar /app/jars/ && \
    cp cart/target/cart.jar /app/jars/ && \
    cp api-gateway/target/api-gateway.jar /app/jars/ && \
    echo "✅ JARs collected:" && \
    ls -lh /app/jars/
