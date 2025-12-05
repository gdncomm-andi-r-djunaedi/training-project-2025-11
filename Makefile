# ===========================================
# Waroenk Microservices - Root Makefile
# ===========================================

.PHONY: help all up down \
        infra infra-down infra-status \
        build \
        backend backend-down backend-parent-down backend-logs \
        api-gateway api-gateway-down api-gateway-logs \
        member member-down member-logs \
        catalog catalog-down catalog-logs \
        cart cart-down cart-logs \
        frontend frontend-build frontend-down frontend-logs \
        logs logs-backend logs-infra \
        status endpoints clean

# Colors
GREEN  := \033[0;32m
YELLOW := \033[0;33m
BLUE   := \033[0;34m
RED    := \033[0;31m
NC     := \033[0m

# Paths
INFRA_PATH=infra
BACKEND_PATH=backend/waroenk-parent
FRONTEND_PATH=frontend

# ===========================================
# Help
# ===========================================
help:
	@echo ""
	@echo "$(BLUE)╔═══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)║        Waroenk Microservices - Control Panel              ║$(NC)"
	@echo "$(BLUE)╚═══════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(GREEN)Full Stack Commands:$(NC)"
	@echo "  make all          - Start everything (infra + backend + frontend)"
	@echo "  make up           - Alias for 'all'"
	@echo "  make down         - Stop everything"
	@echo "  make status       - Show status of all services"
	@echo "  make endpoints    - Print all available endpoints"
	@echo "  make clean        - Stop all and remove volumes/images"
	@echo ""
	@echo "$(GREEN)Build Commands:$(NC)"
	@echo "  make build        - Rebuild ALL backend Docker images (parallel -T 4C)"
	@echo ""
	@echo "$(GREEN)Infrastructure Commands:$(NC)"
	@echo "  make infra        - Start all infrastructure (PostgreSQL, MongoDB, Redis, Typesense)"
	@echo "  make infra-down   - Stop all infrastructure"
	@echo "  make infra-status - Show infrastructure status"
	@echo ""
	@echo "$(GREEN)Backend Commands:$(NC)"
	@echo "  make backend      - Start all backend services (in correct order)"
	@echo "  make backend-down - Stop all backend services"
	@echo ""
	@echo "$(GREEN)Individual Service Commands:$(NC)"
	@echo "  make api-gateway    - Start API Gateway"
	@echo "  make member         - Start Member service"
	@echo "  make catalog        - Start Catalog service"
	@echo "  make cart           - Start Cart service"
	@echo "  make frontend       - Build & Start Frontend (production)"
	@echo "  make frontend-build - Build Frontend image only"
	@echo ""
	@echo "$(GREEN)Logs Commands:$(NC)"
	@echo "  make logs             - Aggregate & follow ALL service logs"
	@echo "  make logs-backend     - Aggregate & follow backend logs only"
	@echo "  make logs-infra       - Aggregate & follow infrastructure logs only"
	@echo "  make api-gateway-logs - View API Gateway logs"
	@echo "  make member-logs      - View Member service logs"
	@echo "  make catalog-logs     - View Catalog service logs"
	@echo "  make cart-logs        - View Cart service logs"
	@echo "  make frontend-logs    - View Frontend logs"
	@echo ""

# ===========================================
# Full Stack Commands
# ===========================================
all: infra backend frontend endpoints
	@echo ""
	@echo "$(GREEN)╔═══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)║           🚀 Waroenk Stack is UP and RUNNING!             ║$(NC)"
	@echo "$(GREEN)╚═══════════════════════════════════════════════════════════╝$(NC)"

up: all

down: frontend-down backend-down backend-parent-down infra-down
	@echo ""
	@echo "$(YELLOW)All services stopped.$(NC)"

# Stop backend services started via parent docker-compose
backend-parent-down:
	@echo "$(YELLOW)Stopping backend services (parent compose)...$(NC)"
	-cd $(BACKEND_PATH) && docker-compose down 2>/dev/null || true

# ===========================================
# Build Commands - Rebuild ALL Backend Docker Images
# ===========================================
build:
	@echo ""
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@echo "$(BLUE)  🐳 Rebuilding ALL Backend Docker Images (NO CACHE)       $(NC)"
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@echo ""
	@echo "$(YELLOW)Step 1: Building shared builder image (Maven -T 4C)...$(NC)"
	cd $(BACKEND_PATH) && docker build --no-cache --progress=plain -f Dockerfile.builder -t waroenk-builder:latest .
	@echo ""
	@echo "$(YELLOW)Step 2: Building service images...$(NC)"
	cd $(BACKEND_PATH)/member && docker build --no-cache --progress=plain -f Dockerfile -t waroenk-parent-member:latest ..
	cd $(BACKEND_PATH)/catalog && docker build --no-cache --progress=plain -f Dockerfile -t waroenk-parent-catalog:latest ..
	cd $(BACKEND_PATH)/cart && docker build --no-cache --progress=plain -f Dockerfile -t waroenk-parent-cart:latest ..
	cd $(BACKEND_PATH)/api-gateway && docker build --no-cache --progress=plain -f Dockerfile -t waroenk-parent-api-gateway:latest ..
	@echo ""
	@echo "$(GREEN)✅ All Docker images built successfully!$(NC)"
	@echo ""
	@docker images | grep -E "(waroenk)" | head -10

# ===========================================
# Infrastructure
# ===========================================
infra:
	@echo ""
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@echo "$(BLUE)  🔧 Starting Infrastructure                              $(NC)"
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@echo ""
	@echo "$(YELLOW)▶ Starting PostgreSQL...$(NC)"
	@$(MAKE) -C $(INFRA_PATH)/postgresql up
	@echo ""
	@echo "$(YELLOW)▶ Starting MongoDB...$(NC)"
	@$(MAKE) -C $(INFRA_PATH)/mongodb up
	@echo ""
	@echo "$(YELLOW)▶ Starting Redis...$(NC)"
	@$(MAKE) -C $(INFRA_PATH)/redis up
	@echo ""
	@echo "$(YELLOW)▶ Starting Typesense...$(NC)"
	@$(MAKE) -C $(INFRA_PATH)/typesense up
	@echo ""
	@echo "$(GREEN)✅ Infrastructure ready!$(NC)"
	@echo ""

infra-down:
	@echo "$(YELLOW)Stopping infrastructure...$(NC)"
	-@$(MAKE) -C $(INFRA_PATH)/redis down
	-@$(MAKE) -C $(INFRA_PATH)/typesense down
	-@$(MAKE) -C $(INFRA_PATH)/mongodb down
	-@$(MAKE) -C $(INFRA_PATH)/postgresql down

infra-status:
	@echo "$(BLUE)Infrastructure Status:$(NC)"
	@docker ps --filter "name=postgres" --filter "name=mongodb" --filter "name=redis" --filter "name=typesense" \
		--format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# ===========================================
# Backend Services
# ===========================================
backend: api-gateway member catalog cart
	@echo ""
	@echo "$(GREEN)✅ All backend services started!$(NC)"

backend-down: cart-down catalog-down member-down api-gateway-down
	@echo "$(YELLOW)Stopping any remaining backend containers...$(NC)"
	-@docker rm -f api-gateway 2>/dev/null || true
	-@docker rm -f member 2>/dev/null || true
	-@docker rm -f catalog 2>/dev/null || true
	-@docker rm -f cart 2>/dev/null || true
	@echo "$(YELLOW)All backend services stopped.$(NC)"

# API Gateway (must start first)
api-gateway:
	@echo ""
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@echo "$(BLUE)  🌐 Starting API Gateway                                 $(NC)"
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@$(MAKE) -C $(BACKEND_PATH)/api-gateway up
	@echo "$(GREEN)⏳ Waiting for API Gateway to be ready...$(NC)"
	@sleep 10

api-gateway-down:
	@echo "$(YELLOW)Stopping API Gateway...$(NC)"
	-@$(MAKE) -C $(BACKEND_PATH)/api-gateway down

api-gateway-logs:
	@$(MAKE) -C $(BACKEND_PATH)/api-gateway logs

# Member Service
member:
	@echo ""
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@echo "$(BLUE)  👤 Starting Member Service                              $(NC)"
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@$(MAKE) -C $(BACKEND_PATH)/member up
	@echo "$(GREEN)⏳ Waiting for Member service to be ready...$(NC)"

member-down:
	@echo "$(YELLOW)Stopping Member service...$(NC)"
	-@$(MAKE) -C $(BACKEND_PATH)/member down

member-logs:
	@$(MAKE) -C $(BACKEND_PATH)/member logs

# Catalog Service
catalog:
	@echo ""
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@echo "$(BLUE)  📦 Starting Catalog Service                             $(NC)"
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@$(MAKE) -C $(BACKEND_PATH)/catalog up
	@echo "$(GREEN)⏳ Waiting for Catalog service to be ready...$(NC)"

catalog-down:
	@echo "$(YELLOW)Stopping Catalog service...$(NC)"
	-@$(MAKE) -C $(BACKEND_PATH)/catalog down

catalog-logs:
	@$(MAKE) -C $(BACKEND_PATH)/catalog logs

# Cart Service
cart:
	@echo ""
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@echo "$(BLUE)  🛒 Starting Cart Service                                $(NC)"
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@$(MAKE) -C $(BACKEND_PATH)/cart up
	@echo "$(GREEN)⏳ Waiting for Cart service to be ready...$(NC)"

cart-down:
	@echo "$(YELLOW)Stopping Cart service...$(NC)"
	-@$(MAKE) -C $(BACKEND_PATH)/cart down

cart-logs:
	@$(MAKE) -C $(BACKEND_PATH)/cart logs

# ===========================================
# Frontend
# ===========================================
frontend-build:
	@echo ""
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@echo "$(BLUE)  🔨 Building Frontend Docker Image                       $(NC)"
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	cd $(FRONTEND_PATH) && docker build --no-cache -t waroenk-frontend:latest --target production .

frontend: frontend-build
	@echo ""
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@echo "$(BLUE)  🎨 Starting Frontend                                    $(NC)"
	@echo "$(BLUE)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(NC)"
	@$(MAKE) -C $(FRONTEND_PATH) docker-prod

frontend-down:
	@echo "$(YELLOW)Stopping Frontend...$(NC)"
	-@$(MAKE) -C $(FRONTEND_PATH) docker-stop
	-@docker rm -f waroenk-frontend 2>/dev/null || true
	-@docker rm -f waroenk-frontend-dev 2>/dev/null || true

frontend-logs:
	@$(MAKE) -C $(FRONTEND_PATH) docker-logs-prod

# ===========================================
# Aggregated Logs
# ===========================================

# Container name patterns for each category
INFRA_CONTAINERS := postgres mongodb redis typesense
BACKEND_CONTAINERS := api-gateway member catalog cart
FRONTEND_CONTAINERS := waroenk-frontend
ALL_CONTAINERS := $(INFRA_CONTAINERS) $(BACKEND_CONTAINERS) $(FRONTEND_CONTAINERS)

# Helper function to get running containers matching our patterns
define get_running_containers
$(shell docker ps --format '{{.Names}}' 2>/dev/null | grep -E '$(1)' || true)
endef

# Aggregate ALL logs from running services
logs:
	@echo ""
	@echo "$(BLUE)╔═══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)║           📋 Aggregated Logs - All Services               ║$(NC)"
	@echo "$(BLUE)╚═══════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(YELLOW)Following logs from all running services (Ctrl+C to stop)...$(NC)"
	@echo ""
	@CONTAINERS=$$(docker ps --format '{{.Names}}' 2>/dev/null | grep -E 'postgres|mongodb|redis|typesense|api-gateway|member|catalog|cart|waroenk-frontend' | tr '\n' ' '); \
	if [ -z "$$CONTAINERS" ]; then \
		echo "$(RED)No running services found. Start services with 'make up' first.$(NC)"; \
	else \
		echo "$(GREEN)Tailing logs from: $$CONTAINERS$(NC)"; \
		echo ""; \
		docker logs -f --tail=50 $$CONTAINERS 2>&1 || \
		for c in $$CONTAINERS; do docker logs -f --tail=50 $$c 2>&1 & done; wait; \
	fi

# Backend logs only
logs-backend:
	@echo ""
	@echo "$(BLUE)╔═══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)║           📋 Aggregated Logs - Backend Services           ║$(NC)"
	@echo "$(BLUE)╚═══════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(YELLOW)Following backend logs (Ctrl+C to stop)...$(NC)"
	@echo ""
	@CONTAINERS=$$(docker ps --format '{{.Names}}' 2>/dev/null | grep -E 'api-gateway|member|catalog|cart' | tr '\n' ' '); \
	if [ -z "$$CONTAINERS" ]; then \
		echo "$(RED)No backend services running. Start with 'make backend' first.$(NC)"; \
	else \
		echo "$(GREEN)Tailing logs from: $$CONTAINERS$(NC)"; \
		echo ""; \
		for c in $$CONTAINERS; do docker logs -f --tail=50 $$c 2>&1 & done; wait; \
	fi

# Infrastructure logs only
logs-infra:
	@echo ""
	@echo "$(BLUE)╔═══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)║           📋 Aggregated Logs - Infrastructure             ║$(NC)"
	@echo "$(BLUE)╚═══════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(YELLOW)Following infrastructure logs (Ctrl+C to stop)...$(NC)"
	@echo ""
	@CONTAINERS=$$(docker ps --format '{{.Names}}' 2>/dev/null | grep -E 'postgres|mongodb|redis|typesense' | tr '\n' ' '); \
	if [ -z "$$CONTAINERS" ]; then \
		echo "$(RED)No infrastructure running. Start with 'make infra' first.$(NC)"; \
	else \
		echo "$(GREEN)Tailing logs from: $$CONTAINERS$(NC)"; \
		echo ""; \
		for c in $$CONTAINERS; do docker logs -f --tail=50 $$c 2>&1 & done; wait; \
	fi

# ===========================================
# Status
# ===========================================
status:
	@echo ""
	@echo "$(BLUE)╔═══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)║               Service Status Overview                     ║$(NC)"
	@echo "$(BLUE)╚═══════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(GREEN)Infrastructure:$(NC)"
	@docker ps --filter "name=postgres" --filter "name=mongodb" --filter "name=redis" --filter "name=typesense" \
		--format "  {{.Names}}\t{{.Status}}" 2>/dev/null || echo "  No infrastructure running"
	@echo ""
	@echo "$(GREEN)Backend Services:$(NC)"
	@docker ps --filter "name=api-gateway" --filter "name=member" --filter "name=catalog" --filter "name=cart" \
		--format "  {{.Names}}\t{{.Status}}" 2>/dev/null || echo "  No backend services running"
	@echo ""
	@echo "$(GREEN)Frontend:$(NC)"
	@docker ps --filter "name=waroenk-frontend" \
		--format "  {{.Names}}\t{{.Status}}" 2>/dev/null || echo "  No frontend running"
	@echo ""

# ===========================================
# Endpoints
# ===========================================
endpoints:
	@echo ""
	@echo "$(BLUE)╔═══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)║               Available Endpoints                         ║$(NC)"
	@echo "$(BLUE)╚═══════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(GREEN)🎨 Frontend:$(NC)"
	@echo "   http://localhost:5173              - Svelte Web App (Dev)"
	@echo ""
	@echo "$(GREEN)🌐 API Gateway (Main Entry Point - Port 8080):$(NC)"
	@echo "   http://localhost:8080              - REST API Gateway"
	@echo "   http://localhost:8080/swagger-ui.html - Swagger UI"
	@echo "   http://localhost:8080/api-docs     - OpenAPI Docs"
	@echo "   http://localhost:8080/health       - Health Check"
	@echo "   http://localhost:8080/routes       - Registered Routes"
	@echo "   http://localhost:8080/services     - Registered Services"
	@echo "   localhost:6565                     - gRPC (Service Registration)"
	@echo ""
	@echo "$(GREEN)👤 Member Service (via Gateway):$(NC)"
	@echo "   http://localhost:8080/api/user/register  - POST Register"
	@echo "   http://localhost:8080/api/user/login     - POST Login"
	@echo "   http://localhost:8080/api/user           - GET/PUT User Profile"
	@echo "   http://localhost:8081/swagger-ui.html    - Direct Swagger (HTTP 8081)"
	@echo "   localhost:9090                           - gRPC Direct"
	@echo ""
	@echo "$(GREEN)📦 Catalog Service (via Gateway):$(NC)"
	@echo "   http://localhost:8080/api/search         - GET Combined Search"
	@echo "   http://localhost:8080/api/search/products - GET Search Products"
	@echo "   http://localhost:8080/api/product        - GET Product by ID"
	@echo "   http://localhost:8080/api/product/filter - GET Filter Products"
	@echo "   http://localhost:8082/swagger-ui.html    - Direct Swagger (HTTP 8082)"
	@echo "   localhost:9091                           - gRPC Direct"
	@echo ""
	@echo "$(GREEN)🛒 Cart Service (via Gateway):$(NC)"
	@echo "   http://localhost:8080/api/cart           - GET Cart (Auth Required)"
	@echo "   http://localhost:8080/api/cart/add       - POST Add Item (Auth Required)"
	@echo "   http://localhost:8080/api/cart/clear     - DELETE Clear Cart (Auth Required)"
	@echo "   http://localhost:8080/api/checkout/*     - Checkout APIs (Auth Required)"
	@echo "   http://localhost:8083/swagger-ui.html    - Direct Swagger (HTTP 8083)"
	@echo "   localhost:9092                           - gRPC Direct"
	@echo ""
	@echo "$(GREEN)🔧 Infrastructure:$(NC)"
	@echo "   localhost:5432                     - PostgreSQL"
	@echo "   localhost:27017                    - MongoDB"
	@echo "   localhost:6379                     - Redis"
	@echo "   http://localhost:8108              - Typesense"
	@echo ""

# ===========================================
# Clean
# ===========================================
clean: down
	@echo ""
	@echo "$(RED)🧹 Cleaning up all resources...$(NC)"
	-@$(MAKE) -C $(FRONTEND_PATH) docker-clean
	-@$(MAKE) -C $(BACKEND_PATH)/cart clean
	-@$(MAKE) -C $(BACKEND_PATH)/catalog clean
	-@$(MAKE) -C $(BACKEND_PATH)/member clean
	-@$(MAKE) -C $(BACKEND_PATH)/api-gateway clean
	-@$(MAKE) -C $(INFRA_PATH)/typesense reset
	-@$(MAKE) -C $(INFRA_PATH)/redis reset
	-@$(MAKE) -C $(INFRA_PATH)/mongodb reset
	-@$(MAKE) -C $(INFRA_PATH)/postgresql reset
	@echo ""
	@echo "$(GREEN)✅ Cleanup complete!$(NC)"
