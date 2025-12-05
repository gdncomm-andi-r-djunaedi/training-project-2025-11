# Product Service

A robust, production-ready Spring Boot microservice for managing e-commerce products with full-text search capabilities using Elasticsearch.

## 🚀 Features

- **Product Management**: Create, update, delete, retrieve, and search products
- **Variant Support**: Products support multiple variants (size, color) with unique SKUs
- **Full-Text Search**: Elasticsearch-powered search with wildcard support, multi-word queries, and relevance scoring
- **Auto-Sync**: Automatic synchronization between MongoDB and Elasticsearch
- **Industry Standard Architecture**:
  - **Layered Architecture**: Controller, Service, Repository, Model
  - **DTO Pattern**: Strict separation between API contracts and Database Entities
  - **Mapper Pattern**: Dedicated mappers for object conversion
- **Performance**:
  - **Caching**: In-memory caching using **Caffeine** for high-performance lookups
  - **Async Operations**: Asynchronous indexing to Elasticsearch
- **Reliability & Observability**:
  - **Global Exception Handling**: Standardized error responses with `GdnResponseData` wrapper
  - **Validation**: Request payload validation using Jakarta Validation
  - **Logging**: Comprehensive SLF4J logging
  - **Actuator**: Health checks and metrics
- **Documentation**: OpenAPI / Swagger UI for interactive API documentation
- **Containerization**: Docker support included

## 🛠️ Tech Stack

- **Java**: 21
- **Framework**: Spring Boot 3.2.0
- **Database**: MongoDB (source of truth)
- **Search Engine**: Elasticsearch 7.17.4 (full-text search)
- **Build Tool**: Maven
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Caching**: Caffeine
- **Utilities**: Lombok, JavaFaker (for data seeding)

## 📦 API Documentation

Once the application is running, access the interactive Swagger UI documentation at:

```
http://localhost:8083/swagger-ui.html
```

### Key Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/products` | Create a new product (Requires `X-User-Role: ROLE_ADMIN`) |
| `GET` | `/api/v1/products/{id}` | Get product details by ID |
| `GET` | `/api/v1/products` | Search products by name and category (Pagination supported) |
| `POST` | `/api/v1/products/search` | Advanced search with Elasticsearch (supports wildcards) |
| `PUT` | `/api/v1/products/{id}` | Update product (Requires `X-User-Role: ROLE_ADMIN`) |
| `DELETE` | `/api/v1/products/{id}` | Delete product (Requires `X-User-Role: ROLE_ADMIN`) |
| `GET` | `/api/v1/internal/products/sku/{sku}` | **Internal**: Lookup product by Variant SKU |

## 🏃‍♂️ Getting Started

### Prerequisites

- Java 21
- Maven
- MongoDB (running on `localhost:27017`)
- Elasticsearch 7.17.4 (optional, see setup below)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd productService
   ```

2. **Build the project**
   ```bash
   ./mvnw clean install
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

The application will start on port **8083**.

### Data Seeding

On the first run, the application will automatically seed **50,000** mock products into the MongoDB database for testing purposes.

## 🔍 Elasticsearch Setup

### Option 1: Using Podman (Recommended for macOS)

1. **Install Podman Desktop**
   ```bash
   brew install --cask podman-desktop
   ```
   Open Podman Desktop from Applications and start the machine.

2. **Start Elasticsearch Container**
   ```bash
   podman pull docker.io/elasticsearch:7.17.4
   
   podman run -d \
     --name elasticsearch \
     -p 9200:9200 \
     -p 9300:9300 \
     -e "discovery.type=single-node" \
     -e "xpack.security.enabled=false" \
     -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
     elasticsearch:7.17.4
   ```

3. **Verify Elasticsearch is running**
   ```bash
   curl http://localhost:9200
   ```

### Option 2: Using Docker

```bash
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  elasticsearch:7.17.4
```

### Option 3: Manual Installation (macOS)

1. **Install via Homebrew**
   ```bash
   brew install elasticsearch-full
   ```

2. **Configure Java 17** (Elasticsearch 7.x requires Java 17)
   ```bash
   export ES_JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.14/libexec/openjdk.jdk/Contents/Home
   export ES_JAVA_OPTS="-Xms512m -Xmx512m"
   ```

3. **Start Elasticsearch**
   ```bash
   /usr/local/opt/elasticsearch-full/bin/elasticsearch
   ```

### Enable/Disable Elasticsearch

Edit `src/main/resources/application.properties`:

```properties
# Enable Elasticsearch (set to false to disable)
spring.data.elasticsearch.repositories.enabled=true
spring.elasticsearch.uris=http://localhost:9200
```

**Note**: The application can run without Elasticsearch. When disabled, it falls back to MongoDB search.

## 🔄 Elasticsearch Auto-Sync

The application automatically keeps MongoDB and Elasticsearch in sync:

### 1. Startup Sync Check
- Compares document counts on startup
- If difference > 5% or Elasticsearch is empty → Triggers full reindex
- Runs asynchronously (doesn't block startup)

### 2. Real-Time Event-Driven Sync
- **Create Product** → Automatically indexes to Elasticsearch
- **Update Product** → Updates Elasticsearch index
- **Delete Product** → Removes from Elasticsearch index

### 3. Scheduled Sync (Fallback)
- Runs every 1 hour
- Checks if MongoDB and Elasticsearch are in sync
- If difference > 10% → Triggers full reindex

## 🔎 Search Features

### Basic Search

```bash
# Search by name
curl "http://localhost:8083/api/v1/products?name=car"

# Search by category
curl "http://localhost:8083/api/v1/products?category=Electronics"

# Search with pagination
curl "http://localhost:8083/api/v1/products?name=phone&page=0&size=20&sort=name,asc"
```

### Multi-Word Search

```bash
# Search for products containing both "Small" AND "Lamp"
curl "http://localhost:8083/api/v1/products?name=Small Lamp"
```

### Wildcard Search

```bash
# Search with prefix wildcard (*phone*)
curl "http://localhost:8083/api/v1/products/search?name=*phone*"

# Search with suffix wildcard (iphone*)
curl "http://localhost:8083/api/v1/products/search?name=iphone*"

# Search with prefix wildcard (*pro)
curl "http://localhost:8083/api/v1/products/search?name=*pro"

# Search with multiple words and wildcards (Aerodynamic * Bag)
curl "http://localhost:8083/api/v1/products/search?name=Aerodynamic * Bag"
```

### Wildcard Characters

- **`*`** - Matches zero or more characters
- **`?`** - Matches exactly one character

### Empty Search (Returns All Products)

```bash
# Get all products
curl "http://localhost:8083/api/v1/products?name="

# Get all products in a category
curl "http://localhost:8083/api/v1/products?category=Electronics"
```

## 📂 Project Structure

```
src/main/java/com/blibli/gdn/productService
├── config          # Configuration classes (OpenAPI, DataSeeder, ElasticsearchSyncConfig)
├── controller      # REST Controllers (Public & Internal)
├── dto             # Data Transfer Objects
│   ├── request     # Request payloads
│   └── response    # Response payloads
├── exception       # Global Exception Handling
├── mapper          # Entity <-> DTO Mappers
├── model           # MongoDB Entities (Product, Variant) & Elasticsearch Documents
├── repository      # MongoDB & Elasticsearch Repositories
└── service         # Business Logic Interfaces & Implementations
    └── impl        # Service Implementations
```

## 🏗️ Architecture

### Hybrid Data Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Request                           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │  ProductService │
                    │  (Spring Boot)  │
                    └────────┬─────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   MongoDB    │    │ Elasticsearch│    │   Caffeine   │
│  (Source of  │    │   (Search)   │    │   (Cache)    │
│   Truth)     │    │              │    │              │
└──────────────┘    └──────────────┘    └──────────────┘
```

### Data Flow

**Product Creation/Update:**
```
Product Created/Updated
    ↓
Save to MongoDB (Primary Storage)
    ↓
Async Index → Elasticsearch (Search Index)
```

**Search Request:**
```
Search Query
    ↓
Query Elasticsearch (Fast Search)
    ↓
Get productIds from search results
    ↓
Fetch full product data from MongoDB
    ↓
Return complete ProductResponse
```

## 🧪 Testing

Run unit and integration tests using Maven:

```bash
./mvnw test
```

## 🐳 Docker Support

1. **Build the Docker image**
   ```bash
   docker build -t product-service .
   ```

2. **Run the container**
   ```bash
   docker run -p 8083:8083 product-service
   ```

## ⚙️ Configuration

### Application Properties

```properties
spring.application.name=productService
spring.data.mongodb.uri=mongodb://localhost:27017/product-service
server.port=8083

# Elasticsearch Configuration
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.connection-timeout=20s
spring.elasticsearch.socket-timeout=60s
spring.data.elasticsearch.repositories.enabled=true

# Caching
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=500,expireAfterAccess=1s
```

## 🔧 Troubleshooting

### Elasticsearch Connection Issues

1. **Check if Elasticsearch is running**
   ```bash
   curl http://localhost:9200
   ```

2. **Check application logs** for connection errors

3. **Disable Elasticsearch** if not needed:
   ```properties
   spring.data.elasticsearch.repositories.enabled=false
   ```

### Search Not Returning Results

1. **Check if products are indexed**
   ```bash
   curl http://localhost:9200/products_index/_count
   ```

2. **Trigger manual reindex** by restarting the application (startup sync will run)

3. **Check logs** for indexing errors

### Out of Memory Errors (Elasticsearch)

If Elasticsearch fails to start with `OutOfMemoryError`:

```bash
# Set lower heap size
export ES_JAVA_OPTS="-Xms512m -Xmx512m"
```

### Podman Issues (macOS)

If you get `vfkit exited unexpectedly` error:
1. Try restarting your Mac
2. Use Podman Desktop GUI instead
3. Check system logs: `log show --predicate 'process == "vfkit"' --last 5m`

## 📊 Performance Considerations

- **Elasticsearch** provides fast full-text search even with 50k+ products
- **MongoDB** is used for exact matches and as the source of truth
- **Caching** improves response times for frequently accessed products
- **Async indexing** ensures product creation/updates don't block API responses

## 🔐 Security

- Product creation, update, and deletion require `X-User-Role: ROLE_ADMIN` header
- Elasticsearch security is disabled by default (development mode)
- For production, enable Elasticsearch security features


