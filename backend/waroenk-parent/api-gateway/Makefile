.PHONY: all build clean up down logs help ensure-network status

# ================================
# Configuration
# ================================
SHARED_NETWORK=shared-network
BACKEND_NETWORK=backend
COMPOSE_FILE=docker-compose.yml
BUILDER_IMAGE=waroenk-builder

# ================================
# Help
# ================================
help:
	@echo "API Gateway Module - Build & Run Commands"
	@echo ""
	@echo "Build Commands:"
	@echo "  make build         Build api-gateway image (requires waroenk-builder)"
	@echo "  make all           Build + start api-gateway"
	@echo "  make clean         Remove built image and volumes"
	@echo ""
	@echo "Service Commands:"
	@echo "  make up            Start api-gateway (uses pre-built image)"
	@echo "  make down          Stop api-gateway"
	@echo "  make logs          View api-gateway service logs"
	@echo ""
	@echo "Info Commands:"
	@echo "  make status        Show service status"
	@echo ""
	@echo "Note: Run 'make build' from root to build all images first."
	@echo ""

# ================================
# Network Management
# ================================
ensure-network:
	@if [ -z "$$(docker network ls --filter name=^$(SHARED_NETWORK)$$ -q)" ]; then \
		echo "➡️  Creating network $(SHARED_NETWORK)"; \
		docker network create $(SHARED_NETWORK); \
	else \
		echo "✔️  Network $(SHARED_NETWORK) already exists"; \
	fi
	@if [ -z "$$(docker network ls --filter name=^$(BACKEND_NETWORK)$$ -q)" ]; then \
		echo "➡️  Creating network $(BACKEND_NETWORK)"; \
		docker network create $(BACKEND_NETWORK); \
	else \
		echo "✔️  Network $(BACKEND_NETWORK) already exists"; \
	fi

# ================================
# Build Commands
# ================================

# Build this service's Docker image (requires waroenk-builder to exist)
# For full rebuild, use root: make build
build: ensure-network
	@if [ -z "$$(docker images -q $(BUILDER_IMAGE):latest 2>/dev/null)" ]; then \
		echo "❌ Builder image not found. Run 'make build' from root first."; \
		exit 1; \
	fi
	@echo "🔨 Building api-gateway service image..."
	DOCKER_BUILDKIT=1 docker build --no-cache -f Dockerfile -t api-gateway:latest ..
	@echo "✅ Done! Image: api-gateway:latest"

# ================================
# Service Commands
# ================================
up: ensure-network
	@echo "🚀 Starting api-gateway service..."
	docker-compose -f $(COMPOSE_FILE) up -d
	@echo ""
	@echo "✅ API Gateway started!"
	@echo "   HTTP REST: http://localhost:8080"
	@echo "   gRPC: localhost:6565"
	@echo "   Swagger: http://localhost:8080/swagger-ui.html"
	@echo "   Health: http://localhost:8080/health"

down:
	@echo "🛑 Stopping api-gateway service..."
	docker-compose -f $(COMPOSE_FILE) down

logs:
	docker-compose -f $(COMPOSE_FILE) logs -f api-gateway

# ================================
# Utility Commands
# ================================
clean:
	@echo "🧹 Cleaning up..."
	docker-compose -f $(COMPOSE_FILE) down -v --rmi all

status:
	@echo "📊 Service Status:"
	@docker-compose -f $(COMPOSE_FILE) ps

# ================================
# All Target
# ================================
all: build up
