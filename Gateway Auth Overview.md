# API Gateway - Authentication, Authorization & Filtering Overview

## Executive Summary

The API Gateway service acts as the **single entry point** for all client requests in the online marketplace platform. It implements **JWT-based authentication and authorization** with a reactive filtering mechanism to secure downstream microservices (Member, Product, Cart).

---

## Architecture Overview

```mermaid
graph TB
    Client[Client Application]
    Gateway[API Gateway :8080]
    Member[Member Service :8083]
    Product[Product Service :8081]
    Cart[Cart Service :8082]
    Redis[(Redis Cache)]
    
    Client -->|1. Login Request| Gateway
    Gateway -->|2. Validate Credentials| Member
    Member -->|3. User Data| Gateway
    Gateway -->|4. Generate JWT| Gateway
    Gateway -->|5. Store in Redis| Redis
    Client -->|6. API Request + JWT| Gateway
    Gateway -->|7. Validate JWT| Redis
    Gateway -->|8. Forward Request| Member
    Gateway -->|8. Forward Request| Product
    Gateway -->|8. Forward Request| Cart
    
    style Gateway fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style Redis fill:#DC382D,stroke:#A02622,color:#fff
```

---

## Core Components

### 1. **JwtAuthenticationFilter** 
**Location**: [`JwtAuthenticationFilter.java`](file:///Users/harikrishnanb/Documents/Qunibay%20Projects/OnlineMarketApplication/training-project-2025-11-Harikrishnan/api-gateway/src/main/java/com/blibi/apigateway/filter/JwtAuthenticationFilter.java)

**Purpose**: Reactive WebFilter that intercepts **all incoming requests** to validate JWT tokens before routing to downstream services.

#### Key Responsibilities:
- **Token Extraction**: Supports both `Authorization: Bearer <token>` header and HTTP-only cookies
- **Blacklist Validation**: Checks Redis for invalidated tokens (logout mechanism)
- **Token Validation**: Verifies signature, expiration, and claims using `JwtUtil`
- **Context Propagation**: Adds authenticated username to exchange attributes for downstream use
- **Path Exclusion**: Bypasses authentication for `/auth/login` and `/api/member/register`

#### Request Flow:
```mermaid
sequenceDiagram
    participant Client
    participant Filter as JwtAuthenticationFilter
    participant Redis
    participant JwtUtil
    participant Downstream as Downstream Service
    
    Client->>Filter: HTTP Request + JWT
    Filter->>Filter: Extract Token (Header/Cookie)
    
    alt No Token Found
        Filter-->>Client: 401 Unauthorized
    end
    
    Filter->>Redis: Check Blacklist (invalid:{token})
    
    alt Token Blacklisted
        Redis-->>Filter: Token Found
        Filter-->>Client: 401 Unauthorized
    end
    
    Filter->>JwtUtil: Validate Token & Check Expiration
    
    alt Invalid/Expired Token
        JwtUtil-->>Filter: Validation Failed
        Filter-->>Client: 401 Unauthorized
    end
    
    JwtUtil-->>Filter: Username Extracted
    Filter->>Filter: Add Username to Exchange Attributes
    Filter->>Downstream: Forward Request
    Downstream-->>Client: Response
```

#### Code Snippet:
```java
@Override
public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();

    // Skip authentication for login endpoint and member registration
    if (path.startsWith("/auth/login") || path.startsWith("/api/member/register")) {
        return chain.filter(exchange);
    }

    String token = extractToken(exchange);
    
    if (token == null || Boolean.TRUE.equals(redisTemplate.hasKey("invalid:" + token))) {
        return unauthorized(exchange);
    }

    // Validate token and add username to exchange
    String username = jwtUtil.validateToken(token);
    exchange.getAttributes().put("username", username);
    
    return chain.filter(exchange);
}
```

---

### 2. **JwtUtil** 
**Location**: [`JwtUtil.java`](file:///Users/harikrishnanb/Documents/Qunibay%20Projects/OnlineMarketApplication/training-project-2025-11-Harikrishnan/api-gateway/src/main/java/com/blibi/apigateway/util/JwtUtil.java)

**Purpose**: Centralized utility for JWT token generation and validation with support for both symmetric (HS256) and asymmetric (RS256) algorithms.

#### Key Features:
- **Algorithm Support**: HS256 (HMAC with SHA-256) and RS256 (RSA with SHA-256)
- **OAuth2 Compliance**: Generates tokens with standard claims (`sub`, `iss`, `aud`, `iat`, `exp`)
- **Custom Claims**: Supports additional claims (e.g., email, active status)
- **Key Management**: Initializes keys from configuration on startup

#### JWT Token Structure:
```json
{
  "sub": "john.doe",           // Subject (username)
  "iss": "api-gateway",        // Issuer
  "aud": "online-marketplace", // Audience
  "iat": 1701878400,          // Issued At (timestamp)
  "exp": 1701964800,          // Expiration (timestamp)
  "email": "john@example.com", // Custom claim
  "active": true              // Custom claim
}
```

#### Token Generation:
```java
public String generateToken(String username, Map<String, Object> additionalClaims) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + jwtProperties.getExpirationMs());

    return Jwts.builder()
        .subject(username)
        .issuer(jwtProperties.getIssuer())
        .audience().add(jwtProperties.getAudience()).and()
        .issuedAt(now)
        .expiration(expiration)
        .claims(additionalClaims)
        .signWith(symmetricKey, Jwts.SIG.HS256) // or rsaPrivateKey for RS256
        .compact();
}
```

#### Token Validation:
```java
public Claims validateAndGetClaims(String token) {
    return Jwts.parser()
        .verifyWith(symmetricKey) // or rsaPublicKey for RS256
        .build()
        .parseSignedClaims(token)
        .getPayload();
}
```

---

### 3. **AuthService & AuthServiceImpl**
**Location**: [`AuthServiceImpl.java`](file:///Users/harikrishnanb/Documents/Qunibay%20Projects/OnlineMarketApplication/training-project-2025-11-Harikrishnan/api-gateway/src/main/java/com/blibi/apigateway/serviceImpl/AuthServiceImpl.java)

**Purpose**: Business logic for authentication operations (login, logout, token validation).

#### Operations:

##### **Login Flow**:
```mermaid
sequenceDiagram
    participant Client
    participant AuthService
    participant WebClient
    participant MemberService
    participant JwtUtil
    participant Redis
    
    Client->>AuthService: POST /auth/login {username, password}
    AuthService->>WebClient: Validate Credentials
    WebClient->>MemberService: POST /api/member/login
    MemberService-->>WebClient: User Data {email, active}
    WebClient-->>AuthService: MemberValidationResponse
    AuthService->>JwtUtil: Generate Token with Claims
    JwtUtil-->>AuthService: JWT Token
    AuthService-->>Client: LoginResponse {token, username}
```

**Code**:
```java
public Mono<LoginResponse> login(LoginRequest request) {
    WebClient webClient = webClientBuilder.baseUrl(memberServiceUrl).build();

    return webClient.post()
        .uri("/api/member/login")
        .bodyValue(validationRequest)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<GenericResponse<MemberValidationResponse>>() {})
        .flatMap(memberResponse -> {
            // Generate JWT with custom claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", memberData.getEmail());
            claims.put("active", memberData.isActive());
            
            String token = jwtUtil.generateToken(request.getUserName(), claims);
            
            return Mono.just(LoginResponse.builder()
                .token(token)
                .userName(memberData.getUserName())
                .build());
        });
}
```

##### **Logout Flow**:
```mermaid
sequenceDiagram
    participant Client
    participant AuthService
    participant JwtUtil
    participant Redis
    
    Client->>AuthService: POST /auth/logout {token}
    AuthService->>JwtUtil: Extract Expiration Time
    JwtUtil-->>AuthService: Expiration Date
    AuthService->>Redis: SET invalid:{token} = "true" TTL={remaining_time}
    Redis-->>AuthService: OK
    AuthService-->>Client: Success Response
```

**Code**:
```java
public void logout(String token) {
    Claims claims = jwtUtil.validateAndGetClaims(token);
    Date expiration = claims.getExpiration();
    long ttlSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;

    if (ttlSeconds > 0) {
        // Blacklist token until natural expiration
        redisTemplate.opsForValue().set("invalid:" + token, "true", ttlSeconds, TimeUnit.SECONDS);
    }
}
```

---

### 4. **AuthController**
**Location**: [`AuthController.java`](file:///Users/harikrishnanb/Documents/Qunibay%20Projects/OnlineMarketApplication/training-project-2025-11-Harikrishnan/api-gateway/src/main/java/com/blibi/apigateway/controller/AuthController.java)

**Purpose**: REST endpoints for authentication operations with dual token delivery (response body + HTTP-only cookie).

#### Endpoints:

| Endpoint | Method | Description | Authentication Required |
|----------|--------|-------------|------------------------|
| `/auth/login` | POST | Authenticate user and issue JWT | ❌ No |
| `/auth/validate` | GET | Validate JWT token | ✅ Yes (token in header/cookie) |
| `/auth/logout` | POST | Invalidate JWT token | ✅ Yes (token in header/cookie) |

#### Login Response with Cookie:
```java
@PostMapping("/auth/login")
public Mono<ResponseEntity<GenericResponse<LoginResponse>>> login(
    @RequestBody LoginRequest request,
    ServerWebExchange exchange) {
    
    return authService.login(request)
        .map(loginResponse -> {
            // Create HTTP-only cookie for enhanced security
            ResponseCookie jwtCookie = ResponseCookie
                .from(jwtProperties.getCookieName(), loginResponse.getToken())
                .httpOnly(true)  // Prevents JavaScript access (XSS protection)
                .secure(false)   // Set to true in production (HTTPS only)
                .path("/")
                .maxAge(86400)   // 24 hours
                .sameSite("Lax") // CSRF protection
                .build();

            exchange.getResponse().addCookie(jwtCookie);
            
            return ResponseEntity.ok(GenericResponse.<LoginResponse>builder()
                .status("SUCCESS")
                .message("Login successful")
                .data(loginResponse)
                .build());
        });
}
```

---

### 5. **Configuration Components**

#### **JwtProperties**
**Location**: [`JwtProperties.java`](file:///Users/harikrishnanb/Documents/Qunibay%20Projects/OnlineMarketApplication/training-project-2025-11-Harikrishnan/api-gateway/src/main/java/com/blibi/apigateway/configuration/JwtProperties.java)

Binds configuration from [`application.properties`](file:///Users/harikrishnanb/Documents/Qunibay%20Projects/OnlineMarketApplication/training-project-2025-11-Harikrishnan/api-gateway/src/main/resources/application.properties):

```properties
# JWT Configuration
jwt.algorithm=HS256
jwt.secret-key=your-256-bit-secret-key-change-this-in-production-environment-please
jwt.expiration-ms=86400000  # 24 hours
jwt.issuer=api-gateway
jwt.audience=online-marketplace

# Cookie Configuration
jwt.cookie-name=JWT_TOKEN
jwt.cookie-http-only=true
jwt.cookie-secure=false  # Set to true in production
jwt.cookie-same-site=Lax
jwt.cookie-max-age=86400  # 24 hours
```

#### **RedisConfig**
**Location**: [`RedisConfig.java`](file:///Users/harikrishnanb/Documents/Qunibay%20Projects/OnlineMarketApplication/training-project-2025-11-Harikrishnan/api-gateway/src/main/java/com/blibi/apigateway/configuration/RedisConfig.java)

Configures Redis for:
- Token blacklist storage (logout)
- Distributed session management
- Caching layer

---

## Security Features

### 1. **Token Blacklisting (Logout Mechanism)**
- **Problem**: JWTs are stateless and cannot be "revoked" once issued
- **Solution**: Maintain a Redis blacklist with keys `invalid:{token}`
- **TTL Strategy**: Blacklist entries expire when the token naturally expires
- **Performance**: O(1) lookup time using Redis

### 2. **Dual Token Delivery**
- **Response Body**: For mobile apps and SPAs that manage tokens in memory
- **HTTP-Only Cookie**: For web browsers to prevent XSS attacks

### 3. **CSRF Protection**
- **SameSite Cookie Attribute**: Set to `Lax` to prevent cross-site request forgery
- **Secure Flag**: Should be enabled in production (HTTPS only)

### 4. **Path-Based Filtering**
Excluded paths (no authentication required):
- `/auth/login` - User authentication endpoint
- `/api/member/register` - New user registration

All other paths require valid JWT token.

---

## Request Flow Examples

### **Successful Authenticated Request**
```
1. Client → Gateway: GET /api/product/search?query=laptop
   Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

2. Gateway (Filter) → Redis: Check blacklist for token
   Redis → Gateway: Token not blacklisted

3. Gateway (Filter) → JwtUtil: Validate token signature and expiration
   JwtUtil → Gateway: Valid, username="john.doe"

4. Gateway → Product Service: GET /api/product/search?query=laptop
   (Adds username to exchange attributes)

5. Product Service → Gateway: 200 OK {products: [...]}

6. Gateway → Client: 200 OK {products: [...]}
```

### **Failed Authentication (Expired Token)**
```
1. Client → Gateway: GET /api/cart/items
   Headers: Authorization: Bearer <expired_token>

2. Gateway (Filter) → JwtUtil: Validate token
   JwtUtil → Gateway: Token expired

3. Gateway → Client: 401 Unauthorized
```

### **Failed Authentication (Blacklisted Token)**
```
1. Client → Gateway: GET /api/member/profile
   Headers: Authorization: Bearer <logged_out_token>

2. Gateway (Filter) → Redis: Check blacklist
   Redis → Gateway: Token found in blacklist

3. Gateway → Client: 401 Unauthorized
```

---

## Gateway Routing Configuration

From [`application.properties`](file:///Users/harikrishnanb/Documents/Qunibay%20Projects/OnlineMarketApplication/training-project-2025-11-Harikrishnan/api-gateway/src/main/resources/application.properties):

```properties
# Cart Service Route
spring.cloud.gateway.routes[0].id=cart
spring.cloud.gateway.routes[0].uri=http://localhost:8082
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/cart/**

# Product Service Route
spring.cloud.gateway.routes[1].id=product
spring.cloud.gateway.routes[1].uri=http://localhost:8081
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/product/**

# Member Service Route
spring.cloud.gateway.routes[2].id=member
spring.cloud.gateway.routes[2].uri=http://localhost:8083
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/member/**
```

**All routes pass through `JwtAuthenticationFilter` before forwarding.**

---

## Key Technologies

| Technology | Purpose |
|------------|---------|
| **Spring Cloud Gateway** | Reactive API Gateway with routing and filtering |
| **Spring WebFlux** | Reactive web framework for non-blocking I/O |
| **JJWT (Java JWT)** | JWT creation and validation library |
| **Redis** | Token blacklist storage and caching |
| **WebClient** | Reactive HTTP client for inter-service communication |
| **Lombok** | Boilerplate code reduction |

---

## Exception Handling

### **GlobalExceptionHandler**
**Location**: [`GlobalExceptionHandler.java`](file:///Users/harikrishnanb/Documents/Qunibay%20Projects/OnlineMarketApplication/training-project-2025-11-Harikrishnan/api-gateway/src/main/java/com/blibi/apigateway/exception/GlobalExceptionHandler.java)

Catches all exceptions and returns standardized error responses:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public GenericResponse<?> handleAny(Exception ex) {
        return GenericResponse.builder()
            .status("ERROR")
            .message(ex.getMessage())
            .build();
    }
}
```

### **UnauthorizedException**
**Location**: [`UnauthorizedException.java`](file:///Users/harikrishnanb/Documents/Qunibay%20Projects/OnlineMarketApplication/training-project-2025-11-Harikrishnan/api-gateway/src/main/java/com/blibi/apigateway/exception/UnauthorizedException.java)

Custom exception for authentication failures:

```java
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```

---

## Best Practices Implemented

✅ **Stateless Authentication**: JWT tokens contain all necessary claims  
✅ **Token Blacklisting**: Proper logout mechanism with Redis  
✅ **Reactive Programming**: Non-blocking I/O for high throughput  
✅ **Security Headers**: HTTP-only cookies, SameSite attribute  
✅ **Centralized Authentication**: Single point of authentication for all services  
✅ **Configurable Algorithms**: Support for both symmetric and asymmetric keys  
✅ **Proper TTL Management**: Blacklist entries expire with tokens  
✅ **Comprehensive Logging**: Debug and error logging throughout  

---

## Topics to Learn

Based on the codebase comments, the following topics are essential:

1. **JWT & JWS**: JSON Web Tokens and JSON Web Signatures
2. **OAuth2**: Industry-standard authorization framework
3. **Reactive Programming**: Project Reactor, Mono, Flux
4. **WebFilter**: Spring Cloud Gateway filtering mechanism
5. **HMAC vs RSA**: Symmetric vs asymmetric cryptography
6. **Redis**: In-memory data structure store
7. **Spring WebFlux**: Reactive web framework
8. **Cookie Security**: HttpOnly, Secure, SameSite attributes
9. **CSRF Protection**: Cross-Site Request Forgery prevention
10. **Microservices Communication**: Service-to-service authentication

---

## Summary

The API Gateway implements a **robust, production-ready authentication and authorization system** with:

- **JWT-based stateless authentication** with OAuth2-compliant claims
- **Reactive filtering** for high-performance request validation
- **Redis-backed token blacklisting** for secure logout
- **Dual token delivery** (header + cookie) for flexibility
- **Path-based access control** with configurable exclusions
- **Centralized security** protecting all downstream microservices

This architecture ensures that **only authenticated requests reach backend services**, while maintaining scalability and security best practices.
