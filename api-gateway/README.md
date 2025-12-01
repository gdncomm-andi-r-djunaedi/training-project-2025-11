# API Gateway

This module is the **central routing layer** for all microservices.

## ✨ Features

- Reverse proxy using `RestTemplate`
- Dynamic routing via `application.yml`
- JWT validation (configurable)
- Rate limiting (configurable per path)
- CORS rules (from config)
- Public vs authenticated routes
- Injects `X-User-Id` header for secure services
- Does NOT throw downstream errors (proxies raw body/status)

---

## 📦 Configuration

### `application.yml` (routes)

```yaml
gateway:
  routes:
    - id: member
      pathPrefix: /api/members
      targetBaseUrl: http://localhost:8081
      targetPathPrefix: /api/members
      requiresAuth: false

    - id: product
      pathPrefix: /api/products
      targetBaseUrl: http://localhost:8082
      targetPathPrefix: /api/products
      requiresAuth: false

    - id: cart
      pathPrefix: /api/cart
      targetBaseUrl: http://localhost:8083
      targetPathPrefix: /api/cart
      requiresAuth: true
```

### Public & Auth paths

```yaml
gateway:
  security:
    public-paths:
      - /swagger-ui/**
      - /v3/api-docs/**
      - /api/members/register
      - /api/members/login
      - /api/products/**

    authenticated-paths:
      - /api/cart/**
      - /api/members/logout
```

### JWT

```yaml
gateway:
  jwt:
    secret: change-this-secret-change-this-secret-1234
    ttlSeconds: 3600
```

### Rate Limiting

```yaml
gateway:
  rate-limit:
    rules:
      - pathPattern: "/api/members/login"
        requests: 10
        windowSeconds: 60
      - pathPattern: "/api/cart/**"
        requests: 50
        windowSeconds: 60
```

### CORS

```yaml
gateway:
  cors:
    mappings:
      - pathPattern: "/api/**"
        allowedOrigins:
          - "http://localhost:3000"
        allowedMethods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
        allowedHeaders: ["*"]
        exposedHeaders: ["X-Total-Count"]
        allowCredentials: true
        maxAge: 3600
```

---

## 🏗 Run

```bash
mvn spring-boot:run
```

Gateway runs on:  
➡ **http://localhost:8080**

