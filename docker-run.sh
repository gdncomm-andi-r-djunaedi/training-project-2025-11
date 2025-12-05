#!/bin/bash
# Docker run script for Linux/Mac
# This script builds and starts all microservices using Docker Compose

echo "========================================"
echo "Marketplace Microservices - Docker"
echo "========================================"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "ERROR: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Parse command line arguments
MODE="up"
BUILD_FLAG="--build"

if [ "$1" = "down" ]; then
    echo "Stopping all services..."
    docker-compose down
    echo ""
    echo "All services stopped."
    exit 0
fi

if [ "$1" = "logs" ]; then
    echo "Showing logs for all services..."
    docker-compose logs -f
    exit 0
fi

if [ "$1" = "rebuild" ]; then
    echo "Rebuilding all services..."
    docker-compose down
    docker-compose build --no-cache
    BUILD_FLAG=""
fi

if [ "$1" = "nobuild" ]; then
    BUILD_FLAG=""
fi

# Start services
echo "Building and starting all services..."
echo "This may take a few minutes on first run..."
echo ""

docker-compose up $BUILD_FLAG -d

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Failed to start services. Check the logs above."
    exit 1
fi

echo ""
echo "========================================"
echo "Services started successfully!"
echo "========================================"
echo ""
echo "API Gateway:     http://localhost:8080"
echo "Member Service:  http://localhost:8081"
echo "Product Service: http://localhost:8082"
echo "Cart Service:    http://localhost:8083"
echo ""
echo "PostgreSQL:      localhost:5432"
echo "MongoDB:         localhost:27017"
echo "Redis:           localhost:6379"
echo ""
echo "To view logs: docker-compose logs -f"
echo "To stop:      docker-compose down"
echo ""
echo "Checking service health..."
sleep 5
docker-compose ps
echo ""
