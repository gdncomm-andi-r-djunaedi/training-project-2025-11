#!/bin/bash

# Online Marketplace Platform - Service Startup Script
# This script starts all 4 microservices in separate terminal tabs (macOS only)

set -e

PROJECT_DIR="/Users/tri.abror/github/training-project-2025-11"

echo "🚀 Starting Online Marketplace Platform Services..."
echo "=================================================="

# Check if Docker/Colima is running
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Docker/Colima is not running!"
    echo "Please start Colima first: colima start --cpu 4 --memory 8"
    exit 1
fi

# Check if databases are running
if ! docker-compose ps | grep -q "marketplace-postgres.*Up"; then
    echo "❌ Database containers are not running!"
    echo "Starting databases..."
    docker-compose up -d
    echo "⏳ Waiting 10 seconds for databases to initialize..."
    sleep 10
fi

echo "✅ Docker/Colima is running"
echo "✅ Database containers are running"
echo ""
echo "Opening terminals for each service..."
echo "Press Ctrl+C in each terminal to stop that service"
echo ""

# Function to open new terminal tab and run command (macOS)
open_terminal() {
    local name=$1
    local directory=$2
    local command=$3
    
    osascript <<EOF
tell application "Terminal"
    activate
    set newTab to do script "cd \"$directory\" && echo '🔷 Starting $name...' && echo '================================================' && $command"
    set custom title of newTab to "$name"
end tell
EOF
}

# Open terminals for each service
open_terminal "API Gateway" "$PROJECT_DIR/api-gateway" "mvn spring-boot:run"
sleep 2

open_terminal "Member Service" "$PROJECT_DIR/member" "mvn spring-boot:run"
sleep 2

open_terminal "Product Service" "$PROJECT_DIR/product" "mvn spring-boot:run"
sleep 2

open_terminal "Cart Service" "$PROJECT_DIR/cart" "mvn spring-boot:run"

echo ""
echo "✅ All services are starting in separate terminal tabs!"
echo ""
echo "Services will be available at:"
echo "  - API Gateway:     http://localhost:8080"
echo "  - Member Service:  http://localhost:8081"
echo "  - Product Service: http://localhost:8082"
echo "  - Cart Service:    http://localhost:8083"
echo ""
echo "Database Management:"
echo "  - PgAdmin:         http://localhost:5050"
echo "  - Mongo Express:   http://localhost:8081"
echo "  - Redis Commander: http://localhost:8082"
echo ""
echo "⏳ Wait ~30-60 seconds for all services to fully start"
echo "📝 Check each terminal tab for 'Started [Service]Application' message"
echo ""
echo "To stop: Press Ctrl+C in each terminal tab"
