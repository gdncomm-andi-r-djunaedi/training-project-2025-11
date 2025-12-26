# Gateway Service - Complete Documentation

API Gateway for the Online Marketplace Platform. Handles routing, authentication, rate limiting, and API aggregation.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Features](#features)
  - [JWT Authentication](#jwt-authentication)
  - [Token Blacklisting](#token-blacklisting)
  - [Rate Limiting](#rate-limiting)
  - [Swagger/OpenAPI Integration](#swaggeropenapi-integration)
  - [Security Configuration](#security-configuration)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Testing Guide](#testing-guide)
- [Rate Limiter Details](#rate-limiter-details)
- [Security Configuration Details](#security-configuration-details)
- [Troubleshooting](#troubleshooting)
- [Production Recommendations](#production-recommendations)

---

## Overview

The Gateway Service is the single entry point for all microservices in the Online Marketplace Platform:

- **Member Service** (Port 8061) - User management
- **Product Service** (Port 8062) - Product catalog
- **Cart Service** (Port 8063) - Shopping cart

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                          API Gateway (8070)                       │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐     │
│  │ 1. CORS Filter (Preflight, Headers)                      │     │
│  └────────────────────────┬──────────────────────────────────┘     │
│                           │                                        │
│  ┌────────────────────────v──────────────────────────────────┐     │
│  │ 2. Authentication Filter (JWT Validation, User Extraction)│     │
│  └────────────────────────┬──────────────────────────────────┘     │
│                           │                                        │
│  ┌────────────────────────v──────────────────────────────────┐     │
│  │ 3. Rate Limiter Filter (Token Bucket, Redis Check)        │     │
│  └────────────────────────┬──────────────────────────────────┘     │
│                           │                                        │
│  ┌────────────────────────v──────────────────────────────────┐     │
│  │ 4. Route to Downstream Service                             │     │
│  └────────────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        v                  v                  v
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│    Member     │  │    Product    │  │     Cart      │
│  Service      │  │   Service     │  │   Service     │
│   (8061)      │  │    (8062)     │  │    (8063)     │
└───────────────┘  └───────────────┘  └───────────────┘
                           
                    ┌───────────────┐
                    │     Redis     │
                    │   (6379)      │
                    │ - Blacklist   │
                    │ - Rate Limits │
                    └───────────────┘
```

---

## Quick Start

### Prerequisites

1. **Java 21** installed
2. **Redis** running on port 6379
3. Backend services running (Member, Product, Cart)

### Start Redis

```bash
redis-cli ping  # Should return PONG
# If not running: redis-server
```

### Start Gateway

```bash
mvn spring-boot:run
```

### Verify

```bash
# Health check
curl http://localhost:8070/actuator/health

# Swagger UI
open http://localhost:8070/webjars/swagger-ui/index.html
```

---

## Features

### JWT Authentication

The Gateway generates and validates JWT tokens using RSA key pairs.

**Authentication Flow:**

```
1. Client → POST /api/v1/auth/login {username, password}
2. Gateway → Forwards to Member Service for validation
3. Member Service → Returns user details
4. Gateway → Generates JWT tokens (accessToken, refreshToken)
5. Gateway → Returns tokens to client
```

**Configuration:**

```properties
jwt.public-key-path=classpath:keys/public_key.pem
jwt.private-key-path=classpath:keys/private_key.pem
jwt.access-token.expiration=300000    # 5 minutes
jwt.refresh-token.expiration=604800000 # 7 days
```

### Token Blacklisting

Tokens are invalidated upon logout using Redis.

**How it works:**
- Tokens stored in Redis with key: `token:blacklist:<jwt_token>`
- TTL automatically calculated based on token expiration
- Prevents use of tokens after logout

**Test logout:**

```bash
curl -X POST 'http://localhost:8070/api/v1/auth/logout' \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Rate Limiting

Redis-backed rate limiting using Token Bucket Algorithm.

**Current Limits:**

| Service | Sustained Rate | Burst Capacity |
|---------|---------------|----------------|
| Member Service | 50 req/sec | 100 requests |
| Product Service | 30 req/sec | 60 requests |
| Cart Service | 40 req/sec | 80 requests |

**Rate Limit Response (429):**

```json
{
  "success": false,
  "status": 429,
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "Rate limit exceeded. Please try again later.",
  "timestamp": "2025-12-05T12:00:00",
  "path": "/api/v1/products/search"
}
```

**Configure per 30 seconds:**

To limit X requests per 30 seconds:
```properties
replenishRate = X / 30
burstCapacity = X
```

Example (300 requests per 30 seconds):
```properties
spring.cloud.gateway.redis-rate-limiter.replenishRate=10
spring.cloud.gateway.redis-rate-limiter.burstCapacity=300
```

### Swagger/OpenAPI Integration

Unified Swagger UI aggregates all services.

**Access:** `http://localhost:8070/webjars/swagger-ui/index.html`

**Services in dropdown:**
1. Gateway Authentication
2. Member Service
3. Product Service
4. Cart Service

**How it works:**
- `OpenApiTransformController` fetches API docs from services (hidden from Swagger UI)
- Transforms server URLs to use Gateway URL
- All API calls go through Gateway

### Security Configuration

Configurable public/secured endpoints in `application.properties`:

```properties
# Public endpoints (no authentication required)
gateway.security.open-endpoints[0]=/api/v1/auth/login
gateway.security.open-endpoints[1]=/api/v1/auth/logout
gateway.security.open-endpoints[2]=/api/v1/member/register
gateway.security.open-endpoints[3]=/api/v1/member/login
gateway.security.open-endpoints[4]=/api/v1/member/logout
gateway.security.open-endpoints[5]=/api/v1/member/members
gateway.security.open-endpoints[6]=/api/v1/member/members/search
gateway.security.open-endpoints[7]=/api/v1/products/search
gateway.security.open-endpoints[8]=/api/v1/products/*
# ... swagger, actuator endpoints
```

**Pattern Matching:**
- Exact match: `/api/v1/member/login`
- Wildcard: `/api/v1/products/**`
- Single segment: `/api/*/health`

---

## Configuration

### application.properties Key Sections

**Server & Redis:**
```properties
server.port=8070
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**Routes:**
```properties
spring.cloud.gateway.routes[2].id=member-service
spring.cloud.gateway.routes[2].uri=http://localhost:8061
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/v1/member/**
spring.cloud.gateway.routes[2].filters[0]=AuthenticationFilter
spring.cloud.gateway.routes[2].filters[1].name=RequestRateLimiter
```

**Rate Limiting:**
```properties
spring.cloud.gateway.redis-rate-limiter.replenishRate=10
spring.cloud.gateway.redis-rate-limiter.burstCapacity=20
```

---

## API Endpoints

### Gateway Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/login` | Login & get JWT | No |
| POST | `/api/v1/auth/logout` | Logout & blacklist token | Yes |

### Member Service (via Gateway)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/member/register` | Register new user | No |
| POST | `/api/v1/member/login` | Login | No |
| POST | `/api/v1/member/logout` | Logout | No |
| GET | `/api/v1/member/members` | List all members | No |
| GET | `/api/v1/member/members/search` | Search members | No |

### Product Service (via Gateway)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/products/search` | Search products | No |
| GET | `/api/v1/products/{id}` | Get product details | No |
| POST | `/api/v1/products` | Create product | Yes |
| PUT | `/api/v1/products/{id}` | Update product | Yes |
| DELETE | `/api/v1/products/{id}` | Delete product | Yes |

### Cart Service (via Gateway)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/cart` | Get cart | Yes |
| POST | `/api/v1/cart/add` | Add to cart | Yes |
| DELETE | `/api/v1/cart/remove` | Remove from cart | Yes |

---

## Testing Guide

### 1. Register a New User

```bash
curl -X POST 'http://localhost:8070/api/v1/member/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "test@example.com",
    "password": "Test@123456"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "username": "test@example.com",
    "email": "test@example.com"
  },
  "status": 200
}
```

### 2. Login

```bash
curl -X POST 'http://localhost:8070/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "test@example.com",
    "password": "Test@123456"
  }'
```

**Save Token:**
```bash
TOKEN=$(curl -s -X POST 'http://localhost:8070/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"test@example.com","password":"Test@123456"}' \
  | jq -r '.data.accessToken')

echo "Token: $TOKEN"
```

### 3. Use Token for Protected Endpoints

```bash
curl -X GET 'http://localhost:8070/api/v1/cart' \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Test Rate Limiting

```bash
for i in {1..70}; do
  curl -s -o /dev/null -w "Request $i: HTTP %{http_code}\n" \
    http://localhost:8070/api/v1/products/search?keyword=test
  sleep 0.05
done
```

**Expected:** First ~60 requests succeed (200), then 429 (Too Many Requests)

### 5. Check Redis

```bash
# Rate limit keys
redis-cli KEYS "request_rate_limiter*"

# Blacklisted tokens
redis-cli KEYS "token:blacklist:*"
```

### 6. Test Public Endpoints (No Auth)

```bash
# Product search - public
curl http://localhost:8070/api/v1/products/search?query=shirt

# Member list - public
curl http://localhost:8070/api/v1/member/members
```

### Complete Test Script

```bash
#!/bin/bash

GATEWAY_URL="http://localhost:8070"
EMAIL="test@example.com"
PASSWORD="Test@123456"

echo "=== Gateway Test Flow ==="

# Register
echo "1. Registering..."
curl -s -X POST "$GATEWAY_URL/api/v1/member/register" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}"

# Login
echo -e "\n\n2. Logging in..."
TOKEN=$(curl -s -X POST "$GATEWAY_URL/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  | jq -r '.data.accessToken')
echo "Token: $TOKEN"

# Use token
echo -e "\n\n3. Accessing protected endpoint..."
curl -s -X GET "$GATEWAY_URL/api/v1/cart" \
  -H "Authorization: Bearer $TOKEN"

# Logout
echo -e "\n\n4. Logging out..."
curl -s -X POST "$GATEWAY_URL/api/v1/auth/logout" \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\n=== Test Complete ==="
```

---

## Rate Limiter Details

### Token Bucket Algorithm

```
┌─────────────────────────────────────────┐
│          Token Bucket                   │
│                                         │
│  Current Tokens: 45 / 100 (burst cap)  │
│                                         │
│  [▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░]               │
│                                         │
│  Replenish Rate: +50 tokens/second     │
│  Time until full: 1.1 seconds          │
└─────────────────────────────────────────┘

Request comes in:
1. Check if tokens available (45 > 0) ✓
2. Deduct 1 token (45 - 1 = 44)
3. Allow request (HTTP 200)

If tokens = 0:
1. Check if tokens available (0 = 0) ✗
2. Reject request (HTTP 429)
3. Add Retry-After header (60 seconds)
```

### Key Parameters

| Parameter | Controls | Unit |
|-----------|----------|------|
| **replenishRate** | Sustained rate | Requests per second |
| **burstCapacity** | Maximum burst | Total requests |
| **requestedTokens** | Cost per request | Tokens |

### Rate Limit Strategies

**1. Combined Strategy (Default)**
- Authenticated users: Limited by username (from JWT)
- Anonymous users: Limited by IP address

**2. IP-based Strategy**
- Limits by client IP
- Good for public endpoints

**3. User-based Strategy**
- Limits by authenticated user (JWT username)
- Fairer for user quotas

**4. Path-based Strategy**
- Limits by endpoint path
- Protects specific endpoints

### Configuration Examples

**Very Strict (Security-Critical):**
```properties
replenishRate=5         # 5 requests per second
burstCapacity=10        # 10 burst
# Use for: Login, password reset
```

**Moderate (Standard API):**
```properties
replenishRate=50        # 50 requests per second
burstCapacity=100       # 100 burst
# Use for: Regular CRUD operations
```

**Generous (Read-Heavy):**
```properties
replenishRate=200       # 200 requests per second
burstCapacity=500       # 500 burst
# Use for: Search, list endpoints
```

### Testing Rate Limits

**Test 1: Exhaust and Wait**
```bash
# Exhaust burst
for i in {1..100}; do
  curl -s http://localhost:8070/api/v1/products/search?keyword=test
done

# Wait for replenishment
sleep 2

# Try again - should allow ~60 requests (30/sec * 2 seconds)
for i in {1..70}; do
  curl -s -o /dev/null -w "Request $i: %{http_code}\n" \
    http://localhost:8070/api/v1/products/search?keyword=test
done
```

**Test 2: Monitor Redis**
```bash
# Check tokens remaining
redis-cli GET "request_rate_limiter.{user:testuser}.tokens"

# Watch in real-time
redis-cli MONITOR | grep request_rate_limiter
```

---

## Security Configuration Details

### Pattern Matching

**Exact Match:**
```properties
gateway.security.open-endpoints[0]=/api/v1/member/login
```
- Matches: `/api/v1/member/login`
- Does NOT match: `/api/v1/member/login/extra`

**Suffix Wildcard (`/**`):**
```properties
gateway.security.open-endpoints[0]=/api/v1/products/**
```
- Matches: `/api/v1/products`, `/api/v1/products/search`, `/api/v1/products/123`

**Single Wildcard (`*`):**
```properties
gateway.security.open-endpoints[0]=/api/*/health
```
- Matches: `/api/v1/health`, `/api/v2/health`

### Common Use Cases

**E-commerce Platform:**
```properties
# Authentication (public)
gateway.security.open-endpoints[0]=/api/v1/member/register
gateway.security.open-endpoints[1]=/api/v1/auth/login

# Product browsing (public)
gateway.security.open-endpoints[2]=/api/v1/products/**

# Cart requires authentication
# Profile requires authentication
```

**API with Public Documentation:**
```properties
# Documentation (public)
gateway.security.open-endpoints[0]=/swagger-ui/**
gateway.security.open-endpoints[1]=/v3/api-docs/**

# Health checks (public)
gateway.security.open-endpoints[2]=/actuator/health

# All other endpoints require authentication
```

### Best Practices

1. **List specific patterns first** for clarity
2. **Use wildcards sparingly** - be explicit when possible
3. **Document why endpoints are public** with comments
4. **Review regularly** - remove public access when no longer needed
5. **Test both authenticated and unauthenticated access**

---

## Troubleshooting

### Gateway won't start
- Check Redis is running: `redis-cli ping`
- Verify port 8070 is available
- Check logs for errors

### Authentication fails
- Verify JWT keys exist in `src/main/resources/keys/`
- Check token expiration
- Ensure Member Service is accessible

### Rate limiting not working
- Verify Redis connection
- Check filter order in application.properties
- Enable debug logging:
  ```properties
  logging.level.org.springframework.cloud.gateway=DEBUG
  logging.level.org.springframework.data.redis=DEBUG
  ```

### Swagger UI shows 404
- Access via: `/webjars/swagger-ui/index.html`
- Check service is running
- Verify routes are configured

### 403 Forbidden from services
- Check service security configuration allows the endpoint pattern
- Ensure patterns match (e.g., `/api/v1/member/**` vs `/api/v1/members/**`)

### 401 Unauthorized on public endpoints
- Verify endpoint is in `gateway.security.open-endpoints` list
- Check pattern matching (exact vs wildcard)
- Ensure array indices are consecutive (0, 1, 2, 3...)

### Rate limit keys not in Redis
- Check Redis connection
- Verify filter is configured on the route
- Make requests to trigger rate limiter

---

## Production Recommendations

### Redis
- Use Redis Cluster for high availability
- Set max memory policy: `maxmemory-policy allkeys-lru`
- Monitor memory usage

### Rate Limiting
- Monitor rate limit metrics with Prometheus/Grafana
- Alert on frequent 429 responses
- Document limits in API docs for consumers
- Provide upgrade path for users needing higher limits

### Security
- Different configs for dev/staging/prod environments
- Rotate JWT keys periodically
- Enable HTTPS
- Use environment-specific `open-endpoints` lists

### Monitoring
- Spring Boot Actuator endpoints
- Gateway metrics
- Health checks
- Custom alerts for:
  - High rate limit rejections
  - Authentication failures
  - Service unavailability

### Performance
- Connection pooling for Redis
- HTTP client tuning
- Circuit breakers for downstream services
- Caching strategies

---

## Project Structure

```
gateway/
├── src/main/java/com/dev/onlineMarketplace/gateway/
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── RateLimiterConfig.java
│   │   ├── RedisConfig.java
│   │   ├── SecurityConfig.java
│   │   └── SwaggerConfig.java
│   ├── controller/
│   │   ├── AuthProxyController.java
│   │   └── OpenApiTransformController.java (Hidden from Swagger)
│   ├── dto/
│   │   ├── LoginRequestDTO.java
│   │   ├── LoginResponseDTO.java
│   │   └── MemberDTO.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   └── GatewayExceptionHandler.java
│   ├── filter/
│   │   ├── AuthenticationFilter.java
│   │   └── RouteValidator.java
│   ├── service/
│   │   └── TokenBlacklistService.java
│   ├── util/
│   │   └── JwtUtil.java
│   └── GatewayApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── keys/
│       ├── private_key.der
│       └── public_key.der
└── pom.xml
```

---

## Version Information

- **Spring Boot**: 3.4.12
- **Spring Cloud**: 2024.0.2
- **Java**: 21
- **SpringDoc OpenAPI**: 2.7.0
- **JJWT**: 0.11.5

---

## Reference Documentation

- [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/reference/)
- [Spring Boot](https://docs.spring.io/spring-boot/3.4.12/reference/)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Redis](https://redis.io/documentation)

---

## Summary

**Gateway is now fully configured with:**
- ✅ JWT Authentication & Token Blacklisting
- ✅ Redis-based Rate Limiting (per-user/per-IP)
- ✅ Configurable Security (public/protected endpoints)
- ✅ Unified Swagger UI (4 services aggregated)
- ✅ CORS enabled
- ✅ Comprehensive error handling

**All services accessible via Gateway:**
- Member Service: `http://localhost:8070/api/v1/member/**`
- Product Service: `http://localhost:8070/api/v1/products/**`
- Cart Service: `http://localhost:8070/api/v1/cart/**`

**Swagger UI:** `http://localhost:8070/webjars/swagger-ui/index.html`
