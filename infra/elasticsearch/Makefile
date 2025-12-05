# ========================================================
# Environment Configuration
# ========================================================
include .env
export $(shell sed 's/=.*//' .env)

ES_CONTAINER=elasticsearch
KIBANA_CONTAINER=kibana

# ========================================================
# Docker Compose Commands
# ========================================================

.PHONY: up down restart logs ps clean

up:
	@echo "🚀 Starting Elasticsearch + Kibana..."
	docker-compose up -d

down:
	@echo "🛑 Stopping stack..."
	docker-compose down

restart:
	@echo "🔄 Restarting all services..."
	docker-compose restart

logs:
	docker-compose logs -f

ps:
	docker-compose ps

clean:
	@echo "🔥 Removing containers, volumes, network..."
	docker-compose down -v --remove-orphans

# ========================================================
# Elasticsearch Security Commands
# ========================================================

password-reset:
	@echo "🔐 Resetting Elasticsearch password..."
	docker exec -it $(ES_CONTAINER) /usr/share/elasticsearch/bin/elasticsearch-reset-password -u $(ELASTIC_USER) -i

token:
	@echo "🎟 Generating Kibana enrollment token..."
	docker exec -it $(ES_CONTAINER) /usr/share/elasticsearch/bin/elasticsearch-create-enrollment-token -s kibana

ca:
	@echo "📄 Printing root SSL certificate..."
	docker exec -it $(ES_CONTAINER) cat /usr/share/elasticsearch/config/certs/http_ca.crt

# ========================================================
# Status & Diagnostics
# ========================================================

health:
	@echo "📊 Checking cluster health..."
	curl -k -u $(ELASTIC_USER):$(ELASTIC_PASSWORD) https://localhost:9200/_cluster/health?pretty

version:
	@echo "🔍 Elasticsearch version:"
	curl -k -u $(ELASTIC_USER):$(ELASTIC_PASSWORD) https://localhost:9200

kibana-version:
	@echo "🔍 Kibana version:"
	curl -k -u $(ELASTIC_USER):$(ELASTIC_PASSWORD) http://localhost:5601/api/status


# ========================================================
# Utility
# ========================================================

fix-permissions:
	@echo "🔧 Fixing folder permissions..."
	sudo chown -R 1000:1000 data elasticsearch kibana certs
