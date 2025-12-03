# Cart Service

E-Commerce Marketplace Cart Service - A Spring Boot microservice for managing shopping carts using MongoDB with Product Service integration.

## Features

- **MongoDB Persistence**: Durable cart storage with TTL-based expiration (30 days)
- **Product Service Integration**: Validates SKUs and enriches cart items with product data using **Feign Client**
- **Cart Synchronization**: Automatically syncs cart item prices/details with Product Service on retrieval
- **Transaction Support**: Atomic cart updates using MongoDB transactions (Requires Replica Set for production)
- **Caching**: Caffeine cache for Product Service responses (1-second TTL)
- **GdnResponseData Wrapper**: Standardized response format
- **Logging**: SLF4J logging for all operations
- **Exception Handling**: @RestControllerAdvice with specific exception handlers
- **OpenAPI Documentation**: Swagger UI at `/swagger-ui.html`
- **Health Checks**: Spring Boot Actuator endpoints

## Tech Stack

- Java 21
- Spring Boot 3.2.0
- MongoDB (with transactions)
- Spring Cloud OpenFeign
- Caffeine Cache
- Lombok
- SpringDoc OpenAPI
- Maven

## Prerequisites

- Java 21
- Maven 3.9+
- MongoDB (running on localhost:27017)
- Product Service (running on localhost:8082)

## Running Locally

### Start MongoDB
```bash
docker run -p 27017:27017 mongo
```

### Run Application
```bash
./mvnw spring-boot:run
```

The service will start on port `8084`.

## API Endpoints

All responses are wrapped in `GdnResponseData`:
```json
{
  "success": true,
  "data": {...},
  "message": "Operation successful",
  "traceId": "uuid"
}
```

### Add to Cart
```bash
POST /api/v1/cart
Content-Type: application/json
X-User-Id: user123

{
  "sku": "AW9-44MM-BLACK-001",
  "qty": 1
}
```

### Get Cart
```bash
GET /api/v1/cart
X-User-Id: user123
```

### Update Quantity
```bash
PUT /api/v1/cart/item/{sku}
Content-Type: application/json
X-User-Id: user123

{
  "qty": 2
}
```

### Remove Item
```bash
DELETE /api/v1/cart/{sku}
X-User-Id: user123
```

### Clear Cart
```bash
DELETE /api/v1/cart
X-User-Id: user123
```

## Error Responses

All errors follow the standard format:
```json
{
  "timestamp": "2025-12-01T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found for SKU: XYZ",
  "path": "/api/v1/cart",
  "details": {},
  "traceId": "abc-123"
}
```

## Docker

### Build Image
```bash
docker build -t cart-service:latest .
```

### Run Container
```bash
docker run -p 8084:8084 \
  -e SPRING_DATA_MONGODB_URI=mongodb://host.docker.internal:27017/ecommerce \
  -e PRODUCT_SERVICE_URL=http://host.docker.internal:8083 \
  cart-service:latest
```

## Testing

```bash
./mvnw test
```

## Documentation

- Swagger UI: http://localhost:8084/swagger-ui.html
- API Docs: http://localhost:8084/v3/api-docs
- Health: http://localhost:8084/actuator/health

## Configuration

Key properties in `application.properties`:
- `server.port=8084`
- `spring.data.mongodb.uri=mongodb://localhost:27017/ecommerce`
- `product.service.url=http://localhost:8083`
- `spring.cache.type=caffeine`

## Architecture

```
Client → API Gateway → Cart Service → MongoDB
                            ↓
                     Product Service
```

Cart Service validates all SKUs with Product Service before adding/updating items.
