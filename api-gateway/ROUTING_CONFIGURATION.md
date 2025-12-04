# Conditional Routing Configuration

## Overview

The API Gateway now supports conditional routing based on authentication requirements. Routes use the standard Spring Cloud Gateway configuration format with an `authenticated` property. Routes marked as `authenticated=true` will be automatically routed through the Internal API Gateway for credential validation.

## Architecture

```
Client Request
    ↓
External API Gateway (Port 8080)
    ↓
    ├─→ If route.authenticated=true → Internal API Gateway (Port 8088) → Service
    └─→ If route.authenticated=false → Service (direct)
```

## Configuration

### External API Gateway (`api-gateway/application.properties`)

Routes are configured using the standard Spring Cloud Gateway format with an additional `authenticated` property:

```properties
# Product Service Route
spring.cloud.gateway.routes[0].id=product-service
spring.cloud.gateway.routes[0].uri=http://localhost:8085
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/product/**
spring.cloud.gateway.routes[0].authenticated=false

# Cart Service Route
spring.cloud.gateway.routes[1].id=cart-service
spring.cloud.gateway.routes[1].uri=http://localhost:8086
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/cart/**
spring.cloud.gateway.routes[1].authenticated=true

# Member Service Route
spring.cloud.gateway.routes[2].id=member-service
spring.cloud.gateway.routes[2].uri=http://localhost:8087
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/member/**
spring.cloud.gateway.routes[2].authenticated=false

# Internal Gateway URL (used when authenticated=true)
gateway.internal-gateway.url=http://localhost:8088
```

### How It Works

1. **Standard Route Configuration**: Routes are defined using Spring Cloud Gateway's standard format
2. **Authenticated Property**: Each route has an `authenticated` boolean property
   - `authenticated=true` → Routes through Internal API Gateway
   - `authenticated=false` → Routes directly to service URI
3. **DynamicRouteLocator**: Reads route configurations from properties and applies conditional routing

## Adding Authentication to a Route

To require authentication for a route:

1. Set `authenticated=true` for the route in `application.properties`:
   ```properties
   spring.cloud.gateway.routes[1].authenticated=true
   ```

2. Ensure the Internal API Gateway has the route configured with `UserValidation` filter

3. Restart the External API Gateway

## Route Configuration Properties

Each route supports the following properties:

- `spring.cloud.gateway.routes[N].id` - Unique route identifier
- `spring.cloud.gateway.routes[N].uri` - Target service URL (used when `authenticated=false`)
- `spring.cloud.gateway.routes[N].predicates[0]` - Path predicate (e.g., `Path=/api/cart/**`)
- `spring.cloud.gateway.routes[N].authenticated` - Boolean flag (true = route through internal gateway, false = direct)

## Example Routes

- `product-service` → `/api/product/**` (authenticated=false)
- `cart-service` → `/api/cart/**` (authenticated=true)
- `member-service` → `/api/member/**` (authenticated=false)

## Internal API Gateway

The Internal API Gateway:
- Validates JWT tokens with Member service
- Extracts user email from validation response
- Adds `X-User-Email` header to requests
- Routes validated requests to target services

## Example Flow

### Authenticated Request (Cart Service)

1. Client sends: `GET /api/cart/{cartId}` with `Authorization: Bearer <token>`
2. External Gateway checks: `cart-service` is in authenticated list
3. External Gateway routes to: Internal Gateway (port 8088)
4. Internal Gateway validates token with Member service
5. Internal Gateway adds `X-User-Email` header
6. Internal Gateway routes to: Cart Service (port 8086)
7. Cart Service receives request with user email in header

### Non-Authenticated Request (Product Service)

1. Client sends: `GET /api/product/{id}`
2. External Gateway checks: `product-service` is NOT in authenticated list
3. External Gateway routes directly to: Product Service (port 8085)

## Benefits

- ✅ **Standard Format**: Uses Spring Cloud Gateway's standard route configuration format
- ✅ **Co-located**: Authentication requirement is defined alongside route configuration
- ✅ **Flexible**: Easy to add/remove authentication per route by changing one property
- ✅ **Centralized**: Authentication logic in one place (Internal Gateway)
- ✅ **Configurable**: No code changes needed, just update properties
- ✅ **Scalable**: Can add more authenticated routes easily
- ✅ **Maintainable**: All route information in one place per route

