# Microservices Architecture Diagram

## Complete System Architecture

```mermaid
graph TB
    subgraph Client["👤 CLIENT"]
        C[Browser/Postman/Mobile App]
    end

    subgraph Gateway["🚪 API GATEWAY (Port 8999)"]
        SC[SecurityConfig<br/>- Define public/protected routes]
        JF[JwtFilter<br/>- Validate JWT tokens<br/>- Extract userId]
        CF[CartUserIdGatewayFilter<br/>- Inject X-User-Id header]
        AC[AuthController<br/>- Handle login]
        LF[LogInFeign<br/>- Feign client for Member Service]
        JS[JWTService<br/>- Generate tokens<br/>- Validate tokens]
        GR[Gateway Router<br/>- Route requests to services]
    end

    subgraph MemberService["👥 MEMBER SERVICE (Port 8081)"]
        MS[Member Service API]
        MP[(PostgreSQL<br/>Member Database)]
    end

    subgraph ProductService["📦 PRODUCT SERVICE (Port 8004)"]
        PS[Product Service API]
        PP[(PostgreSQL<br/>Product Database)]
    end

    subgraph CartService["🛒 CART SERVICE (Port 8085)"]
        CS[Cart Service API]
        CM[(MongoDB<br/>Cart Database)]
    end

    subgraph SearchService["🔍 SEARCH SERVICE"]
        SS[Search Service API]
        ES[(Elasticsearch<br/>Search Index)]
    end

    subgraph MessageQueue["📨 KAFKA"]
        K[Kafka Message Broker]
    end

    %% Client to Gateway
    C -->|1. POST /api/member/register<br/>email, password| Gateway
    C -->|2. POST /api/member/logIn<br/>email, password| Gateway
    C -->|3. GET /api/products/**<br/>No token required| Gateway
    C -->|4. POST /api/cart/items<br/>Authorization: Bearer token| Gateway

    %% Gateway Internal Flow - Registration
    Gateway -->|Route to member-service| MS
    MS -->|Store in database| MP
    MP -->|Return success| MS
    MS -->|Return response| Gateway
    Gateway -->|Return to client| C

    %% Gateway Internal Flow - Login
    AC -->|Feign Call| LF
    LF -->|POST /api/member/logIn| MS
    MS -->|Validate credentials| MP
    MP -->|Return: isMember=true, userId| MS
    MS -->|Return response| LF
    LF -->|Return to AuthController| AC
    AC -->|If valid, generate token| JS
    JS -->|Return JWT token| AC
    AC -->|Return token to client| C

    %% Gateway Internal Flow - Product Access
    Gateway -->|Route directly| PS
    PS -->|Query products| PP
    PP -->|Return products| PS
    PS -->|Return response| Gateway
    Gateway -->|Return to client| C

    %% Gateway Internal Flow - Cart Access (Authenticated)
    Gateway -->|Validate token| JF
    JF -->|Token valid?| CF
    CF -->|Extract userId from token<br/>Add X-User-Id header| GR
    GR -->|Forward request with<br/>X-User-Id header| CS
    CS -->|Query cart by userId| CM
    CM -->|Return cart data| CS
    CS -->|Return response| Gateway
    Gateway -->|Return to client| C

    %% Product to Search via Kafka
    PS -->|Publish product update event| K
    K -->|Consume event| SS
    SS -->|Index in Elasticsearch| ES

    %% Cart to Product (Feign call)
    CS -.->|Feign Call<br/>Get product details| PS
    PS -.->|Return product info| CS

    %% Styling
    classDef gateway fill:#9b59b6,stroke:#8e44ad,stroke-width:3px,color:#fff
    classDef service fill:#3498db,stroke:#2980b9,stroke-width:2px,color:#fff
    classDef database fill:#e74c3c,stroke:#c0392b,stroke-width:2px,color:#fff
    classDef client fill:#2ecc71,stroke:#27ae60,stroke-width:2px,color:#fff
    classDef queue fill:#f39c12,stroke:#d68910,stroke-width:2px,color:#fff

    class SC,JF,CF,AC,LF,JS,GR gateway
    class MS,PS,CS,SS service
    class MP,PP,CM,ES database
    class C client
    class K queue
```

## Detailed Flow Diagrams

### 1. Registration Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant G as API Gateway
    participant MS as Member Service
    participant DB as PostgreSQL

    C->>G: POST /api/member/register<br/>{email, password}
    Note over G: SecurityConfig: permitAll()<br/>No authentication required
    G->>G: Gateway Router matches<br/>/api/member/register
    G->>MS: Forward request to<br/>http://localhost:8081/api/member/register
    MS->>DB: INSERT INTO members<br/>(email, password_hash)
    DB-->>MS: Success
    MS-->>G: HTTP 201 Created<br/>{success: true}
    G-->>C: HTTP 201 Created<br/>{success: true}
```

### 2. Login Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant G as API Gateway
    participant AC as AuthController
    participant LF as LogInFeign
    participant MS as Member Service
    participant DB as PostgreSQL
    participant JS as JWTService

    C->>G: POST /api/member/logIn<br/>{email, password}
    Note over G: SecurityConfig: permitAll()<br/>No authentication required
    G->>AC: Route to AuthController
    AC->>LF: Feign call to member-service
    LF->>MS: POST /api/member/logIn<br/>{email, password}
    MS->>DB: SELECT * FROM members<br/>WHERE email = ? AND password = ?
    alt Valid Credentials
        DB-->>MS: Return member data<br/>{isMember: true, userId: 10988}
        MS-->>LF: {success: true, data: {isMember: true, userId: 10988}}
        LF-->>AC: Response received
        AC->>JS: generateToken(userId: 10988)
        JS->>JS: Create JWT with:<br/>- sub: "10988"<br/>- iat: current_time<br/>- exp: current_time + 1hr<br/>- Sign with secret key
        JS-->>AC: JWT token: "eyJhbGc..."
        AC-->>G: HTTP 200 OK<br/>{token: "eyJhbGc...", message: "Login successful"}
        G-->>C: HTTP 200 OK<br/>{token: "eyJhbGc...", message: "Login successful"}
    else Invalid Credentials
        DB-->>MS: No matching member
        MS-->>LF: {success: false, data: null}
        LF-->>AC: Response received
        AC-->>G: HTTP 401 Unauthorized
        G-->>C: HTTP 401 Unauthorized<br/>"Invalid credentials"
    end
```

### 3. Product Access Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant G as API Gateway
    participant PS as Product Service
    participant DB as PostgreSQL

    C->>G: GET /api/products/**<br/>No token required
    Note over G: SecurityConfig: permitAll()<br/>No authentication required
    G->>G: Gateway Router matches<br/>/api/products/**
    G->>PS: Forward request to<br/>http://localhost:8004/api/products/**
    PS->>DB: SELECT * FROM products
    DB-->>PS: Return products
    PS-->>G: HTTP 200 OK<br/>[{product1}, {product2}, ...]
    G-->>C: HTTP 200 OK<br/>[{product1}, {product2}, ...]
```

### 4. Product Update & Search Indexing Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant G as API Gateway
    participant PS as Product Service
    participant DB as PostgreSQL
    participant K as Kafka
    participant SS as Search Service
    participant ES as Elasticsearch

    C->>G: PUT /api/products/{id}<br/>{name, price, description}
    Note over G: SecurityConfig: permitAll()<br/>No authentication required
    G->>PS: Forward request to<br/>http://localhost:8004/api/products/{id}
    PS->>DB: UPDATE products SET ...<br/>WHERE id = ?
    DB-->>PS: Update successful
    PS->>K: Publish event:<br/>{eventType: "PRODUCT_UPDATED",<br/>productId: 123, data: {...}}
    PS-->>G: HTTP 200 OK<br/>{success: true}
    G-->>C: HTTP 200 OK<br/>{success: true}
    
    Note over K,ES: Asynchronous Processing
    K->>SS: Consume event:<br/>PRODUCT_UPDATED
    SS->>ES: Index/Update document<br/>in Elasticsearch
    ES-->>SS: Index updated
```

### 5. Cart Access Flow (Authenticated)

```mermaid
sequenceDiagram
    participant C as Client
    participant G as API Gateway
    participant SC as SecurityConfig
    participant JF as JwtFilter
    participant CF as CartUserIdGatewayFilter
    participant GR as Gateway Router
    participant CS as Cart Service
    participant DB as MongoDB

    C->>G: POST /api/cart/items<br/>Authorization: Bearer eyJhbGc...<br/>Body: {productId: 1, quantity: 2}
    
    Note over G: SecurityConfig checks path
    SC->>SC: /api/cart/** requires authentication
    
    Note over G: Filter Chain Execution
    G->>JF: JwtFilter.doFilterInternal()
    JF->>JF: Extract token from<br/>Authorization header
    JF->>JF: Validate signature<br/>using secret key
    JF->>JF: Check expiration<br/>(not expired)
    JF->>JF: Extract userId from<br/>token subject: "10988"
    JF->>JF: Set SecurityContextHolder<br/>with authentication
    JF-->>G: Token valid, continue
    
    G->>CF: CartUserIdGatewayFilter.doFilterInternal()
    CF->>CF: Get authentication from<br/>SecurityContextHolder
    CF->>CF: Extract userId: 10988
    CF->>CF: Wrap request with<br/>X-User-Id: 10988 header
    CF-->>G: Request wrapped
    
    G->>GR: Gateway Router matches<br/>/api/cart/**
    GR->>CS: Forward request to<br/>http://localhost:8085/api/cart/items<br/>Headers: X-User-Id: 10988
    
    CS->>CS: Extract userId from<br/>X-User-Id header
    CS->>DB: INSERT/UPDATE cart<br/>WHERE userId = 10988
    DB-->>CS: Cart updated
    CS-->>GR: HTTP 201 Created<br/>{cart: {...}}
    GR-->>G: Response received
    G-->>C: HTTP 201 Created<br/>{cart: {...}}
```

### 6. Cart to Product Feign Call Flow

```mermaid
sequenceDiagram
    participant CS as Cart Service
    participant PS as Product Service
    participant DB as PostgreSQL

    Note over CS: When adding item to cart,<br/>Cart Service needs product details
    CS->>PS: Feign Call:<br/>GET /api/products/{productId}
    PS->>DB: SELECT * FROM products<br/>WHERE id = ?
    DB-->>PS: Return product data
    PS-->>CS: HTTP 200 OK<br/>{id: 1, name: "Product", price: 99.99}
    CS->>CS: Use product data to<br/>calculate cart totals
```

## Component Details

### API Gateway Components

| Component | Purpose | Key Methods |
|-----------|---------|-------------|
| **SecurityConfig** | Configures Spring Security, defines public/protected routes, sets up filter chain | `securityFilterChain()` |
| **JwtFilter** | Validates JWT tokens, extracts userId, sets authentication context | `doFilterInternal()`, `shouldNotFilter()` |
| **CartUserIdGatewayFilter** | Extracts userId from JWT and injects X-User-Id header for cart requests | `doFilterInternal()`, `shouldNotFilter()` |
| **AuthController** | Handles login requests, calls member-service via Feign, generates JWT tokens | `login()` |
| **LogInFeign** | Feign client interface for calling member-service login endpoint | `logIn()` |
| **JWTService** | Generates and validates JWT tokens, extracts claims | `generateToken()`, `extractUserId()`, `isTokenExpired()` |
| **Gateway Router** | Routes requests to appropriate microservices based on path patterns | Configured in `application.properties` |

### Service Ports

- **API Gateway**: 8999
- **Member Service**: 8081
- **Product Service**: 8004
- **Cart Service**: 8085

### Database Technologies

- **Member Service**: PostgreSQL
- **Product Service**: PostgreSQL
- **Cart Service**: MongoDB
- **Search Service**: Elasticsearch

### Communication Patterns

1. **Synchronous HTTP**: Gateway to services, Cart to Product (Feign)
2. **Asynchronous Messaging**: Product to Search via Kafka
3. **Stateless Authentication**: JWT tokens, no server-side sessions

## Security Flow Summary

```
┌─────────────────────────────────────────────────────────────┐
│                    REQUEST CLASSIFICATION                    │
└─────────────────────────────────────────────────────────────┘
                              │
                ┌─────────────┴─────────────┐
                │                           │
        ┌───────▼───────┐         ┌─────────▼─────────┐
        │  PUBLIC PATH  │         │  PROTECTED PATH   │
        │               │         │                   │
        │ - /register   │         │ - /api/cart/**    │
        │ - /logIn      │         │                   │
        │ - /products/**│         │ Requires:          │
        │               │         │ - JWT Token        │
        │ No auth needed│         │ - Valid signature │
        │               │         │ - Not expired     │
        └───────┬───────┘         │ - userId extracted│
                │                 └─────────┬─────────┘
                │                           │
                │                 ┌─────────▼─────────┐
                │                 │   JwtFilter       │
                │                 │ - Validate token  │
                │                 │ - Set auth context│
                │                 └─────────┬─────────┘
                │                           │
                │                 ┌─────────▼─────────┐
                │                 │CartUserIdGateway  │
                │                 │Filter (cart only) │
                │                 │ - Extract userId  │
                │                 │ - Add X-User-Id   │
                │                 └─────────┬─────────┘
                │                           │
                └───────────┬───────────────┘
                            │
                    ┌───────▼───────┐
                    │ Route to      │
                    │ Microservice  │
                    └───────────────┘
```

## Key Design Decisions

1. **Stateless Authentication**: JWT tokens eliminate need for server-side sessions
2. **Gateway as Single Entry Point**: All client requests go through gateway
3. **Service-Specific Databases**: Each service has its own database (polyglot persistence)
4. **Event-Driven Updates**: Product changes propagated to Search via Kafka
5. **Header-Based User Context**: Cart service receives userId via X-User-Id header
6. **Feign for Inter-Service Communication**: Declarative HTTP client for synchronous calls



