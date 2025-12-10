# Lightweight API Gateway Implementation (Spring Boot 3 + gRPC)

## 1. Overview

A **lightweight, memory-optimized API Gateway** using **Spring Boot 3** that:

* Accepts HTTP/HTTPS requests from clients (REST)
* Routes requests to microservices via **gRPC**
* Performs **JWT-based authentication and authorization**
* Uses **static configuration** from application.properties (no database!)
* Uses **gRPC Server Reflection** for dynamic type discovery
* Handles **error translation** (gRPC → HTTP)

**Key Features:**
- ✅ **No PostgreSQL** - Routes loaded from properties at startup
- ✅ **No Redis** - In-memory route cache only
- ✅ **No scheduled tasks** - No heartbeat/cleanup needed
- ✅ **No client registration** - Static configuration only
- ✅ **Low memory footprint** - ~128-256MB heap
- ✅ **Fast startup** - No database migrations or connections

---

## 2. Architecture

```
HTTP Client (Svelte UI)
        |
        v
   API GATEWAY (Spring Boot)
 ┌───────────────────────────────────────────────┐
 | HTTP REST API (:8080)                         |
 |   - JWT Validation                            |
 |   - Route Resolution (from properties)        |
 |   - JSON ↔ Proto Translation                  |
 | ─────────────────────────────────────────────|
 | gRPC Server Reflection Client                 |
 |   - Dynamic method descriptor discovery       |
 |   - Cached with Caffeine (in-memory)          |
 | ─────────────────────────────────────────────|
 | Storage Layer                                 |
 |   - In-memory HashMap (routes)                |
 |   - Caffeine cache (reflection descriptors)   |
 └───────────────────────────────────────────────┘
        |
        v
  gRPC Microservices
 ┌─────────┐ ┌─────────┐ ┌─────────┐
 │ Member  │ │ Catalog │ │  Cart   │
 └─────────┘ └─────────┘ └─────────┘
```

---

## 3. Project Structure

```
api-gateway/
├── src/main/java/com/gdn/project/waroenk/gateway/
│   ├── ApiGatewayApplication.java
│   ├── config/
│   │   ├── GatewayProperties.java      # Route & service config
│   │   ├── GrpcChannelConfig.java      # gRPC channel management
│   │   ├── SecurityConfig.java         # JWT + Spring Security
│   │   ├── JacksonConfig.java          # JSON serialization
│   │   └── SwaggerConfig.java          # OpenAPI docs
│   ├── controller/
│   │   ├── GatewayController.java      # Dynamic /api/** routing
│   │   ├── HealthController.java       # /health, /info, /routes, /services
│   │   ├── MonitoringController.java   # /monitoring/** endpoints
│   │   ├── DashboardController.java    # Dashboard UI
│   │   └── ControllerAdvice.java       # Global exception handler
│   ├── service/
│   │   ├── StaticRouteRegistry.java    # In-memory route storage
│   │   ├── RouteResolver.java          # Route resolution
│   │   ├── GrpcProxyService.java       # gRPC invocation
│   │   ├── ReflectionGrpcClient.java   # gRPC Server Reflection
│   │   └── MonitoringService.java      # Service health checks
│   ├── dto/
│   │   ├── ErrorResponseDto.java
│   │   ├── GatewayResponse.java
│   │   └── monitoring/
│   │       ├── DashboardSummaryDto.java
│   │       ├── ServiceHealthDto.java
│   │       └── ServiceInfoDto.java
│   ├── exception/
│   │   ├── GatewayException.java
│   │   ├── RouteNotFoundException.java
│   │   └── ServiceUnavailableException.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtUtil.java
│   │   └── UserPrincipal.java
│   └── utility/
│       └── ExceptionTranslatorUtil.java
├── src/main/resources/
│   ├── application.properties          # All routes defined here!
│   └── static/
│       └── dashboard.html              # Monitoring dashboard
├── Dockerfile
├── docker-compose.yml
└── Makefile
```

---

## 4. Configuration

### 4.1 Service Endpoints

```properties
# gRPC Service Endpoints
gateway.services.member.host=${MEMBER_GRPC_HOST:localhost}
gateway.services.member.port=${MEMBER_GRPC_PORT:9090}
gateway.services.member.http-port=${MEMBER_HTTP_PORT:8081}
gateway.services.member.use-tls=false

gateway.services.catalog.host=${CATALOG_GRPC_HOST:localhost}
gateway.services.catalog.port=${CATALOG_GRPC_PORT:9091}
gateway.services.catalog.http-port=${CATALOG_HTTP_PORT:8082}
gateway.services.catalog.use-tls=false

gateway.services.cart.host=${CART_GRPC_HOST:localhost}
gateway.services.cart.port=${CART_GRPC_PORT:9092}
gateway.services.cart.http-port=${CART_HTTP_PORT:8083}
gateway.services.cart.use-tls=false
```

### 4.2 Route Configuration

```properties
# User Routes (Member Service)
gateway.routes[0].path=/api/user/**
gateway.routes[0].service=member
gateway.routes[0].grpc-service=member.user.UserService
gateway.routes[0].public-route=false

gateway.routes[0].methods[0].http-method=POST
gateway.routes[0].methods[0].http-path=/api/user/register
gateway.routes[0].methods[0].grpc-method=Register
gateway.routes[0].methods[0].public-endpoint=true

gateway.routes[0].methods[1].http-method=POST
gateway.routes[0].methods[1].http-path=/api/user/login
gateway.routes[0].methods[1].grpc-method=Authenticate
gateway.routes[0].methods[1].public-endpoint=true

gateway.routes[0].methods[2].http-method=GET
gateway.routes[0].methods[2].http-path=/api/user
gateway.routes[0].methods[2].grpc-method=GetOneUserById
```

### 4.3 JWT Configuration

```properties
gateway.jwt.secret=${JWT_SECRET:base64-encoded-secret}
gateway.jwt.access-token-expiration=3600
gateway.jwt.refresh-token-expiration=604800
```

---

## 5. Memory Optimization

### 5.1 What Was Removed

| Component | Memory Saved | Notes |
|-----------|--------------|-------|
| PostgreSQL | ~50-100MB | No JDBC connection pool |
| Redis | ~30-50MB | No Lettuce client |
| JPA/Hibernate | ~50-80MB | No entity metadata |
| Flyway | ~10-20MB | No migration infrastructure |
| gRPC Registration Server | ~20-30MB | No gRPC server for registration |
| Scheduled Tasks | ~5-10MB | No @Scheduled threads |

### 5.2 JVM Settings

```bash
# Recommended for production
JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
```

### 5.3 Docker Resource Limits

```yaml
deploy:
  resources:
    limits:
      memory: 256M
    reservations:
      memory: 128M
```

---

## 6. How It Works

### 6.1 Request Flow

```
1. HTTP Request arrives at GatewayController
        |
2. JwtAuthenticationFilter validates token (if protected endpoint)
        |
3. RouteResolver looks up route in StaticRouteRegistry (HashMap)
        |
4. GrpcProxyService prepares the request:
   - Extract path variables
   - Merge query params
   - Inject user context (for protected endpoints)
        |
5. ReflectionGrpcClient invokes the gRPC method:
   - Uses gRPC Server Reflection to discover method descriptors
   - Caches descriptors in Caffeine (30 min TTL)
   - Converts JSON → DynamicMessage → gRPC call
        |
6. Response converted: DynamicMessage → JSON
        |
7. HTTP Response returned
```

### 6.2 gRPC Server Reflection

The gateway uses **gRPC Server Reflection** to dynamically discover method descriptors at runtime. This means:

- **No need to specify request/response types** in configuration
- **No need to generate Java classes** for each proto message
- **Service-agnostic** - works with any gRPC service that has reflection enabled
- **Cached** - Descriptors cached in Caffeine for performance

```java
// ReflectionGrpcClient uses DynamicMessage for JSON ↔ Protobuf conversion
DynamicMessage.Builder requestBuilder = DynamicMessage.newBuilder(methodInfo.inputType());
jsonParser.merge(jsonRequest, requestBuilder);
DynamicMessage request = requestBuilder.build();
```

---

## 7. API Endpoints

### 7.1 Health & Info

| Endpoint | Description |
|----------|-------------|
| `/health` | Gateway health status |
| `/info` | Gateway info + route/service counts |
| `/services` | List all configured services |
| `/routes` | List all configured routes |
| `/routes/summary` | Routes grouped by service |

### 7.2 Monitoring

| Endpoint | Description |
|----------|-------------|
| `/monitoring/dashboard` | Service health summary |
| `/monitoring/services` | All services health status |
| `/monitoring/services/{name}` | Specific service info |
| `/monitoring/stats` | Route statistics |
| `/dashboard` | Dashboard HTML page |

---

## 8. Running

### Development (Local)

```bash
cd api-gateway

# Compile
./mvnw clean compile

# Run (from parent directory)
cd ..
./mvnw spring-boot:run -pl api-gateway
```

### Docker

```bash
cd api-gateway

# Build image
docker build -t waroenk-parent-api-gateway:latest .

# Run with docker-compose
docker-compose up -d

# Check logs
docker-compose logs -f

# Check health
curl http://localhost:8080/health
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| HTTP_REST_PORT | 8080 | HTTP server port |
| JWT_SECRET | (base64) | JWT signing secret |
| MEMBER_GRPC_HOST | localhost | Member service host |
| MEMBER_GRPC_PORT | 9090 | Member service gRPC port |
| CATALOG_GRPC_HOST | localhost | Catalog service host |
| CATALOG_GRPC_PORT | 9091 | Catalog service gRPC port |
| CART_GRPC_HOST | localhost | Cart service host |
| CART_GRPC_PORT | 9092 | Cart service gRPC port |

---

## 9. Error Handling

### gRPC → HTTP Status Mapping

| gRPC Status | HTTP Status | Error Code |
|-------------|-------------|------------|
| NOT_FOUND | 404 | RESOURCE_NOT_FOUND |
| ALREADY_EXISTS | 409 | RESOURCE_EXISTS |
| INVALID_ARGUMENT | 400 | INVALID_ARGUMENT |
| UNAUTHENTICATED | 401 | UNAUTHENTICATED |
| PERMISSION_DENIED | 403 | PERMISSION_DENIED |
| UNAVAILABLE | 503 | SERVICE_UNAVAILABLE |
| INTERNAL | 500 | INTERNAL_ERROR |

### Error Response Format

```json
{
  "status": 404,
  "message": "NOT_FOUND",
  "details": "No microservice registered for GET /api/orders",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 10. Adding New Routes

To add a new route, simply add to `application.properties`:

```properties
# New Payment Service
gateway.services.payment.host=${PAYMENT_GRPC_HOST:localhost}
gateway.services.payment.port=${PAYMENT_GRPC_PORT:9093}
gateway.services.payment.http-port=${PAYMENT_HTTP_PORT:8084}
gateway.services.payment.use-tls=false

# Payment Routes
gateway.routes[9].path=/api/payment/**
gateway.routes[9].service=payment
gateway.routes[9].grpc-service=payment.PaymentService
gateway.routes[9].public-route=false

gateway.routes[9].methods[0].http-method=POST
gateway.routes[9].methods[0].http-path=/api/payment/process
gateway.routes[9].methods[0].grpc-method=ProcessPayment
```

Then restart the gateway. No code changes needed!

---

## 11. Comparison: Old vs New Architecture

| Aspect | Old (Dynamic) | New (Static) |
|--------|---------------|--------------|
| Memory | 512MB+ | 128-256MB |
| Startup Time | 30-60s | 10-15s |
| External Dependencies | PostgreSQL, Redis | None |
| Configuration | Database + Properties | Properties only |
| Route Changes | Runtime (API) | Restart required |
| Complexity | High | Low |
| Maintenance | Database migrations | Properties changes |

---

## 12. Future Enhancements

- [ ] Circuit breaker (Resilience4j)
- [ ] Rate limiting per client/service
- [ ] Request/response logging
- [ ] OpenTelemetry tracing
- [ ] Hot reload of properties (Spring Cloud Config)
