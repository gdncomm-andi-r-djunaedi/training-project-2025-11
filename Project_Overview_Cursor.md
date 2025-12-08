# Online Marketplace Application - Design Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Overview](#architecture-overview)
3. [Microservices Details](#microservices-details)
4. [Data Flow Diagrams](#data-flow-diagrams)
5. [Sequence Diagrams](#sequence-diagrams)
6. [API Gateway Communication](#api-gateway-communication)
7. [Technology Stack](#technology-stack)
8. [Database Schemas](#database-schemas)
9. [Security Architecture](#security-architecture)
10. [API Endpoints](#api-endpoints)

---

## System Overview

The Online Marketplace Application is a microservices-based e-commerce platform built with Spring Boot. The system consists of four main microservices:

- **API Gateway** (Port 8080): Single entry point, authentication, and request routing
- **Member Service** (Port 8083): User management and authentication
- **Product Service** (Port 8081): Product catalog management
- **Cart Service** (Port 8082): Shopping cart management

### Key Features
- JWT-based authentication with token blacklisting
- Reactive API Gateway using Spring Cloud Gateway
- Inter-service communication via Feign Client and WebClient
- MongoDB for product and cart data
- PostgreSQL for member data
- Redis for caching and token blacklisting

---

## Architecture Overview

### High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        Client[Postman]
    end
    
    subgraph "API Gateway Layer"
        Gateway[API Gateway<br/>Port 8080<br/>Spring Cloud Gateway]
        JWTFilter[JWT Authentication Filter]
        AuthService[Auth Service]
        RedisGW[(Redis<br/>Token Blacklist)]
    end
    
    subgraph "Microservices Layer"
        Member[Member Service<br/>Port 8083<br/>Spring Boot]
        Product[Product Service<br/>Port 8081<br/>Spring Boot]
        Cart[Cart Service<br/>Port 8082<br/>Spring Boot]
    end
    
    subgraph "Data Layer"
        PostgreSQL[(PostgreSQL<br/>Member Data)]
        MongoDB1[(MongoDB<br/>Product Data)]
        MongoDB2[(MongoDB<br/>Cart Data)]
        RedisProduct[(Redis<br/>Product Cache)]
    end
    
    Client -->|HTTP Requests| Gateway
    Gateway --> JWTFilter
    JWTFilter --> RedisGW
    JWTFilter --> AuthService
    Gateway -->|Route /api/member/**| Member
    Gateway -->|Route /api/product/**| Product
    Gateway -->|Route /api/cart/**| Cart
    Gateway -->|WebClient| Member
    Member --> PostgreSQL
    Product --> MongoDB1
    Product --> RedisProduct
    Cart --> MongoDB2
    Cart -->|Feign Client| Product
    
    style Gateway fill:#4A90E2,color:#fff
    style Member fill:#50C878,color:#fff
    style Product fill:#F5A623,color:#fff
    style Cart fill:#BD10E0,color:#fff
```

### Component Architecture

```mermaid
graph LR
    subgraph "API Gateway"
        GW_Controller[Auth Controller]
        GW_Service[Auth Service]
        GW_Filter[JWT Filter]
        GW_Util[JWT Util]
        GW_Config[Gateway Config]
    end
    
    subgraph "Member Service"
        M_Controller[Member Controller]
        M_Service[Member Service]
        M_Repository[Member Repository]
        M_Security[Security Config]
    end
    
    subgraph "Product Service"
        P_Controller[Product Controller]
        P_Service[Product Service]
        P_Repository[Product Repository]
        P_Cache[Redis Cache]
    end
    
    subgraph "Cart Service"
        C_Controller[Cart Controller]
        C_Service[Cart Service]
        C_Repository[Cart Repository]
        C_Feign[Product Feign Client]
    end
    
    GW_Controller --> GW_Service
    GW_Service --> GW_Util
    GW_Filter --> GW_Util
    GW_Filter --> GW_Config
    
    M_Controller --> M_Service
    M_Service --> M_Repository
    M_Service --> M_Security
    
    P_Controller --> P_Service
    P_Service --> P_Repository
    P_Service --> P_Cache
    
    C_Controller --> C_Service
    C_Service --> C_Repository
    C_Service --> C_Feign
    C_Feign -.->|HTTP Call| P_Service
```

---

## Microservices Details

### 1. API Gateway Service

**Port:** 8080  
**Framework:** Spring Cloud Gateway (Reactive)  
**Database:** Redis (Token Blacklist)

#### Responsibilities
- Single entry point for all client requests
- JWT token generation and validation
- Request routing to backend services
- Token blacklisting on logout
- Cookie-based and Bearer token authentication support

#### Key Components

**AuthController**
- `/auth/login` - User authentication and JWT generation
- `/auth/logout` - Token invalidation
- `/auth/validate` - Token validation

**JwtAuthenticationFilter**
- Validates JWT tokens on incoming requests
- Extracts tokens from Authorization header or cookies
- Checks Redis blacklist
- Adds username to request attributes for downstream services

**AuthServiceImpl**
- Uses WebClient for direct HTTP calls to Member Service
- Generates JWT tokens with custom claims
- Manages token blacklisting in Redis

**JwtUtil**
- JWT token generation (HS256 algorithm)
- Token validation and claim extraction
- OAuth 2.0 compatible claims (sub, iss, aud, iat, exp)

#### Configuration
```properties
# Gateway Routes
spring.cloud.gateway.routes[0].id=cart
spring.cloud.gateway.routes[0].uri=http://localhost:8082
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/cart/**

spring.cloud.gateway.routes[1].id=product
spring.cloud.gateway.routes[1].uri=http://localhost:8081
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/product/**

spring.cloud.gateway.routes[2].id=member
spring.cloud.gateway.routes[2].uri=http://localhost:8083
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/member/**
```

---

### 2. Member Service

**Port:** 8083  
**Framework:** Spring Boot Web  
**Database:** PostgreSQL

#### Responsibilities
- User registration with password hashing
- Login credential validation
- User profile management
- Password encryption using BCrypt

#### Key Components

**MemberController**
- `POST /api/member/register` - Register new user
- `POST /api/member/login` - Validate credentials

**MemberServiceImpl**
- Password hashing with BCrypt
- Email uniqueness validation
- User authentication

**Member Entity**
- UUID primary key
- Fields: userName, password (hashed), email, isActive

#### Database Schema
```sql
CREATE TABLE member_detail (
    id UUID PRIMARY KEY,
    user_name VARCHAR(255),
    password VARCHAR(255), -- BCrypt hashed
    email VARCHAR(255),
    is_active BOOLEAN
);
```

---

### 3. Product Service

**Port:** 8081  
**Framework:** Spring Boot Web  
**Database:** MongoDB  
**Cache:** Redis

#### Responsibilities
- Product catalog management
- Product search and filtering
- Product creation and retrieval
- Redis caching for performance

#### Key Components

**ProductController**
- `POST /api/product/createProduct` - Create new product
- `GET /api/product/productDetail/productName/{name}` - Get by exact name
- `POST /api/product/searchProduct/productName/{name}` - Wildcard search
- `GET /api/product/searchProduct/category/{category}` - Search by category
- `GET /api/product/{id}` - Get product details

**ProductServiceImpl**
- Product CRUD operations
- Case-insensitive search using MongoDB regex
- Pagination support
- Entity-DTO conversion

**Product Entity**
- MongoDB document with indexes
- Fields: id, productId (unique), productName, description, price, category, images

#### MongoDB Indexes
```javascript
// Compound indexes
{ productName: 1 } // Non-unique
{ productId: 1 }   // Unique
```

---

### 4. Cart Service

**Port:** 8082  
**Framework:** Spring Boot Web  
**Database:** MongoDB  
**Communication:** Feign Client

#### Responsibilities
- Shopping cart management
- Add/remove items from cart
- Cart retrieval
- Product price fetching via Feign Client
- Quantity aggregation for duplicate products

#### Key Components

**CartController**
- `POST /api/cart/{userId}/add` - Add item to cart
- `GET /api/cart/{userId}` - View cart
- `DELETE /api/cart/{userId}/remove/{productId}` - Remove item

**CartServiceImpl**
- Cart creation if user doesn't have one
- Product validation via Feign Client
- Quantity updates for existing products
- Total price calculation

**ProductFeignClient**
- Declarative REST client for Product Service
- Fetches product details by ID
- Handles service-to-service communication

**Cart Entity**
- UUID as document ID (userId)
- Embedded CartItem documents
- Total cart price field

---

## Data Flow Diagrams

### 1. User Registration Flow

```mermaid
flowchart TD
    Start([User Registration Request]) --> Gateway[API Gateway<br/>Port 8080]
    Gateway --> CheckAuth{Authentication<br/>Required?}
    CheckAuth -->|No| Route[Route to Member Service]
    Route --> Member[Member Service<br/>Port 8083]
    Member --> Validate[Validate Email Uniqueness]
    Validate --> Hash[Hash Password with BCrypt]
    Hash --> Save[Save to PostgreSQL]
    Save --> Response[Return Success Response]
    Response --> Gateway
    Gateway --> End([Response to Client])
    
    style Gateway fill:#4A90E2,color:#fff
    style Member fill:#50C878,color:#fff
```

### 2. User Login Flow

```mermaid
flowchart TD
    Start([Login Request]) --> Gateway[API Gateway<br/>AuthController]
    Gateway --> AuthService[AuthService]
    AuthService --> WebClient[WebClient HTTP Call]
    WebClient --> Member[Member Service<br/>/api/member/login]
    Member --> Validate[Validate Credentials]
    Validate -->|Invalid| Error[Return Error]
    Validate -->|Valid| MemberResponse[Return Member Data]
    MemberResponse --> AuthService
    AuthService --> GenerateJWT[Generate JWT Token]
    GenerateJWT --> SetCookie[Set JWT Cookie]
    SetCookie --> Response[Return Login Response]
    Response --> End([Client Receives Token])
    
    style Gateway fill:#4A90E2,color:#fff
    style Member fill:#50C878,color:#fff
```

### 3. Authenticated Request Flow

```mermaid
flowchart TD
    Start([Authenticated Request]) --> Gateway[API Gateway]
    Gateway --> JWTFilter[JWT Authentication Filter]
    JWTFilter --> ExtractToken[Extract Token from<br/>Header or Cookie]
    ExtractToken --> CheckBlacklist{Token in<br/>Redis Blacklist?}
    CheckBlacklist -->|Yes| Unauthorized[401 Unauthorized]
    CheckBlacklist -->|No| ValidateToken[Validate Token Signature]
    ValidateToken -->|Invalid| Unauthorized
    ValidateToken -->|Valid| CheckExpiry{Token<br/>Expired?}
    CheckExpiry -->|Yes| Unauthorized
    CheckExpiry -->|No| AddUsername[Add Username to<br/>Request Attributes]
    AddUsername --> Route[Route to Backend Service]
    Route --> Service[Backend Service<br/>Member/Product/Cart]
    Service --> Process[Process Request]
    Process --> Response[Return Response]
    Response --> End([Response to Client])
    
    style Gateway fill:#4A90E2,color:#fff
    style JWTFilter fill:#FF6B6B,color:#fff
```

### 4. Add to Cart Flow

```mermaid
flowchart TD
    Start([Add to Cart Request]) --> Gateway[API Gateway]
    Gateway --> JWTFilter[JWT Filter<br/>Validate Token]
    JWTFilter --> Route[Route to Cart Service]
    Route --> CartController[Cart Controller]
    CartController --> CartService[Cart Service]
    CartService --> FeignClient[Product Feign Client]
    FeignClient --> ProductService[Product Service<br/>GET /api/product/{id}]
    ProductService --> MongoDB[(MongoDB<br/>Product Data)]
    MongoDB --> ProductService
    ProductService --> ProductResponse[Product Details]
    ProductResponse --> FeignClient
    FeignClient --> CartService
    CartService --> CheckCart{Cart Exists?}
    CheckCart -->|No| CreateCart[Create New Cart]
    CheckCart -->|Yes| GetCart[Get Existing Cart]
    CreateCart --> AddItem[Add/Update Cart Item]
    GetCart --> AddItem
    AddItem --> CalculateTotal[Calculate Total Price]
    CalculateTotal --> SaveCart[Save to MongoDB]
    SaveCart --> CartResponse[Return Cart Response]
    CartResponse --> End([Response to Client])
    
    style Gateway fill:#4A90E2,color:#fff
    style CartService fill:#BD10E0,color:#fff
    style ProductService fill:#F5A623,color:#fff
```

---

## Sequence Diagrams

### 1. User Registration Sequence

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant Member as Member Service
    participant DB as PostgreSQL
    
    Client->>Gateway: POST /api/member/register
    Note over Gateway: JWT Filter: Skip auth<br/>for registration
    Gateway->>Member: Forward request
    Member->>DB: Check email exists
    DB-->>Member: Email check result
    alt Email exists
        Member-->>Gateway: Error: Email already exists
        Gateway-->>Client: 400 Bad Request
    else Email available
        Member->>Member: Hash password (BCrypt)
        Member->>DB: Save member
        DB-->>Member: Member saved
        Member-->>Gateway: Success response
        Gateway-->>Client: 200 OK with member data
    end
```

### 2. User Login Sequence

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant AuthService as Auth Service
    participant Member as Member Service
    participant DB as PostgreSQL
    participant JWTUtil as JWT Util
    
    Client->>Gateway: POST /auth/login
    Gateway->>AuthService: Process login
    AuthService->>Member: POST /api/member/login<br/>(WebClient)
    Member->>DB: Find user by username
    DB-->>Member: User data
    Member->>Member: Validate password (BCrypt)
    alt Invalid credentials
        Member-->>AuthService: Error response
        AuthService-->>Gateway: UnauthorizedException
        Gateway-->>Client: 401 Unauthorized
    else Valid credentials
        Member-->>AuthService: Member data
        AuthService->>JWTUtil: Generate JWT token
        JWTUtil-->>AuthService: JWT token
        AuthService->>Gateway: Login response with token
        Gateway->>Gateway: Set JWT cookie
        Gateway-->>Client: 200 OK with token
    end
```

### 3. Authenticated Product Search Sequence

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant JWTFilter as JWT Filter
    participant Redis as Redis
    participant Product as Product Service
    participant MongoDB as MongoDB
    
    Client->>Gateway: GET /api/product/search/laptop<br/>(JWT Token)
    Gateway->>JWTFilter: Filter request
    JWTFilter->>JWTFilter: Extract token
    JWTFilter->>Redis: Check blacklist
    Redis-->>JWTFilter: Not blacklisted
    JWTFilter->>JWTFilter: Validate token signature
    JWTFilter->>JWTFilter: Check expiration
    JWTFilter->>Gateway: Add username to attributes
    Gateway->>Product: Forward request
    Product->>MongoDB: Search products
    MongoDB-->>Product: Product results
    Product->>Product: Convert to DTO
    Product-->>Gateway: Paginated products
    Gateway-->>Client: 200 OK with products
```

### 4. Add to Cart Sequence

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant Cart as Cart Service
    participant Feign as Feign Client
    participant Product as Product Service
    participant MongoDB as MongoDB
    
    Client->>Gateway: POST /api/cart/{userId}/add<br/>(JWT Token)
    Gateway->>Gateway: Validate JWT
    Gateway->>Cart: Forward request
    Cart->>Feign: Get product details
    Feign->>Product: GET /api/product/{id}
    Product->>MongoDB: Find product
    MongoDB-->>Product: Product data
    Product-->>Feign: Product response
    Feign-->>Cart: Product details
    Cart->>MongoDB: Find cart by userId
    MongoDB-->>Cart: Cart (or null)
    alt Cart exists
        Cart->>Cart: Update existing item
    else Cart not found
        Cart->>Cart: Create new cart
    end
    Cart->>Cart: Calculate total price
    Cart->>MongoDB: Save cart
    MongoDB-->>Cart: Cart saved
    Cart-->>Gateway: Cart response
    Gateway-->>Client: 200 OK with cart
```

### 5. Logout Sequence

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant AuthService as Auth Service
    participant JWTUtil as JWT Util
    participant Redis as Redis
    
    Client->>Gateway: POST /auth/logout<br/>(JWT Token)
    Gateway->>Gateway: Extract token
    Gateway->>AuthService: Logout(token)
    AuthService->>JWTUtil: Get token expiration
    JWTUtil-->>AuthService: Expiration date
    AuthService->>AuthService: Calculate TTL
    AuthService->>Redis: Add to blacklist<br/>invalid:{token}
    Redis-->>AuthService: Token blacklisted
    AuthService->>Gateway: Logout success
    Gateway->>Gateway: Clear JWT cookie
    Gateway-->>Client: 200 OK
```

---

## API Gateway Communication

### Communication Patterns

The API Gateway uses three different communication patterns to interact with backend services:

#### 1. Gateway → Member Service (Direct WebClient)

**Purpose:** Authentication operations  
**Method:** Reactive WebClient (HTTP)  
**Endpoint:** `http://localhost:8083/api/member/login`

**Implementation:**
```java
// AuthServiceImpl.java
WebClient webClient = webClientBuilder.baseUrl(memberServiceUrl).build();
return webClient.post()
    .uri("/api/member/login")
    .bodyValue(validationRequest)
    .retrieve()
    .bodyToMono(MemberValidationResponse.class)
```

**Flow:**
1. Client sends login request to Gateway
2. Gateway's AuthService uses WebClient
3. Direct HTTP POST to Member Service
4. Member Service validates credentials
5. Response returned to Gateway
6. Gateway generates JWT token

**Diagram:**
```mermaid
graph LR
    Client[Client] -->|POST /auth/login| Gateway[API Gateway]
    Gateway -->|WebClient POST| Member[Member Service<br/>Port 8083]
    Member -->|Validate| PostgreSQL[(PostgreSQL)]
    PostgreSQL -->|User Data| Member
    Member -->|Member Response| Gateway
    Gateway -->|Generate JWT| Gateway
    Gateway -->|Login Response| Client
    
    style Gateway fill:#4A90E2,color:#fff
    style Member fill:#50C878,color:#fff
```

#### 2. Gateway → Product Service (Spring Cloud Gateway Routing)

**Purpose:** Product catalog access  
**Method:** Transparent proxy routing  
**Pattern:** Path-based routing

**Configuration:**
```properties
spring.cloud.gateway.routes[1].id=product
spring.cloud.gateway.routes[1].uri=http://localhost:8081
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/product/**
```

**Request Transformation:**
- Original: `GET http://localhost:8080/api/product/search/laptop`
- Routed to: `GET http://localhost:8081/api/product/search/laptop`
- Headers preserved, JWT validated
- Username added to request attributes

**Diagram:**
```mermaid
graph LR
    Client[Client] -->|GET /api/product/search/laptop<br/>JWT Token| Gateway[API Gateway<br/>Port 8080]
    Gateway -->|1. JWT Validation| Filter[JWT Filter]
    Filter -->|2. Route Match| Router[Gateway Router]
    Router -->|3. Forward to<br/>http://localhost:8081| Product[Product Service<br/>Port 8081]
    Product -->|4. Response| Router
    Router -->|5. Response| Client
    
    style Gateway fill:#4A90E2,color:#fff
    style Product fill:#F5A623,color:#fff
```

#### 3. Gateway → Cart Service (Spring Cloud Gateway Routing)

**Purpose:** Shopping cart operations  
**Method:** Transparent proxy routing  
**Pattern:** Path-based routing with authentication

**Configuration:**
```properties
spring.cloud.gateway.routes[0].id=cart
spring.cloud.gateway.routes[0].uri=http://localhost:8082
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/cart/**
```

**Flow:**
1. Client sends request with JWT token
2. Gateway validates JWT
3. Gateway extracts username
4. Gateway routes to Cart Service
5. Cart Service processes request
6. Cart Service may call Product Service via Feign

**Diagram:**
```mermaid
graph LR
    Client[Client] -->|POST /api/cart/add<br/>JWT Token| Gateway[API Gateway]
    Gateway -->|1. Validate JWT| Filter[JWT Filter]
    Filter -->|2. Extract username| Filter
    Filter -->|3. Add to attributes| Gateway
    Gateway -->|4. Route to Cart| Cart[Cart Service<br/>Port 8082]
    Cart -->|5. Get product info| Feign[Feign Client]
    Feign -->|6. HTTP GET| Product[Product Service]
    Product -->|7. Product details| Feign
    Feign -->|8. Return| Cart
    Cart -->|9. Save cart| MongoDB[(MongoDB)]
    Cart -->|10. Response| Gateway
    Gateway -->|11. Response| Client
    
    style Gateway fill:#4A90E2,color:#fff
    style Cart fill:#BD10E0,color:#fff
    style Product fill:#F5A623,color:#fff
```

### JWT Authentication Filter Flow

```mermaid
flowchart TD
    Start([Incoming Request]) --> CheckPath{Path requires<br/>authentication?}
    CheckPath -->|/auth/login or<br/>/api/member/register| SkipAuth[Skip Authentication]
    CheckPath -->|Other paths| ExtractToken[Extract Token from<br/>Header or Cookie]
    SkipAuth --> Allow[Allow Request]
    ExtractToken --> TokenExists{Token<br/>Found?}
    TokenExists -->|No| Unauthorized[401 Unauthorized]
    TokenExists -->|Yes| CheckBlacklist{Token in<br/>Redis Blacklist?}
    CheckBlacklist -->|Yes| Unauthorized
    CheckBlacklist -->|No| ValidateSignature[Validate Token<br/>Signature]
    ValidateSignature -->|Invalid| Unauthorized
    ValidateSignature -->|Valid| CheckExpiry{Token<br/>Expired?}
    CheckExpiry -->|Yes| Unauthorized
    CheckExpiry -->|No| AddUsername[Add Username to<br/>Exchange Attributes]
    AddUsername --> Allow
    Allow --> Route[Route to Backend Service]
    
    style Unauthorized fill:#FF6B6B,color:#fff
    style Allow fill:#50C878,color:#fff
```

### Gateway Routing Decision Tree

```mermaid
flowchart TD
    Start([Request Arrives at Gateway]) --> CheckPath[Check Request Path]
    CheckPath -->|/auth/**| AuthEndpoint[Auth Controller]
    CheckPath -->|/api/member/**| MemberRoute[Route to Member Service<br/>Port 8083]
    CheckPath -->|/api/product/**| ProductRoute[Route to Product Service<br/>Port 8081]
    CheckPath -->|/api/cart/**| CartRoute[Route to Cart Service<br/>Port 8082]
    
    AuthEndpoint --> AuthLogic[Auth Service Logic]
    AuthLogic -->|Login| WebClientCall[WebClient to Member Service]
    AuthLogic -->|Logout| BlacklistToken[Blacklist Token in Redis]
    AuthLogic -->|Validate| ValidateToken[Validate Token]
    
    MemberRoute --> JWTFilter1[JWT Filter]
    ProductRoute --> JWTFilter2[JWT Filter]
    CartRoute --> JWTFilter3[JWT Filter]
    
    JWTFilter1 --> MemberService[Member Service]
    JWTFilter2 --> ProductService[Product Service]
    JWTFilter3 --> CartService[Cart Service]
    
    style AuthEndpoint fill:#4A90E2,color:#fff
    style MemberRoute fill:#50C878,color:#fff
    style ProductRoute fill:#F5A623,color:#fff
    style CartRoute fill:#BD10E0,color:#fff
```

---

## Technology Stack

### Backend Framework
- **Spring Boot:** 3.4.12
- **Java:** 21
- **Build Tool:** Maven

### Microservices
- **API Gateway:** Spring Cloud Gateway, Spring WebFlux (Reactive)
- **Member Service:** Spring Boot Web, Spring Security
- **Product Service:** Spring Boot Web, Spring Data MongoDB
- **Cart Service:** Spring Boot Web, Spring Data MongoDB, Spring Cloud OpenFeign

### Databases
- **PostgreSQL:** Member data storage
- **MongoDB:** Product and Cart data storage
- **Redis:** Token blacklisting (Gateway) and caching (Product Service)

### Security
- **JWT:** JSON Web Tokens (HS256 algorithm)
- **BCrypt:** Password hashing
- **Spring Security:** Authentication and authorization

### Inter-Service Communication
- **Spring Cloud Gateway:** Request routing
- **WebClient:** Reactive HTTP client (Gateway → Member)
- **Feign Client:** Declarative REST client (Cart → Product)

### Additional Libraries
- **JJWT:** JWT token generation and validation
- **Lombok:** Code generation
- **SpringDoc OpenAPI:** API documentation

---

## Database Schemas

### PostgreSQL (Member Service)

**Table: member_detail**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, AUTO GENERATED | Unique member identifier |
| user_name | VARCHAR(255) | NOT NULL | Username for login |
| password | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Member email address |
| is_active | BOOLEAN | NOT NULL | Account active status |

**Indexes:**
- Primary key on `id`
- Unique constraint on `email`

### MongoDB (Product Service)

**Collection: products**

```javascript
{
  "_id": ObjectId("..."),           // MongoDB generated ID
  "productId": "MTA-000001",        // Business product ID (unique)
  "productName": "Laptop Pro 15",   // Product name
  "description": "High-performance...", // Product description
  "price": 1299.99,                 // Product price
  "category": "Electronics",         // Product category
  "images": [                        // Product images array
    "https://example.com/image1.jpg"
  ]
}
```

**Indexes:**
- Compound index on `productName` (non-unique)
- Unique index on `productId`

### MongoDB (Cart Service)

**Collection: cart**

```javascript
{
  "_id": UUID("550e8400-..."),      // User ID (also cart ID)
  "cartId": UUID("550e8400-..."),   // Same as _id
  "totalCartPrice": 2599.98,         // Total cart price
  "items": [                         // Array of cart items
    {
      "productId": "MTA-000001",
      "quantity": 2,
      "itemTotalPrice": 2599.98
    }
  ]
}
```

**Indexes:**
- Primary key on `_id` (UUID)

### Redis (API Gateway)

**Key Pattern:** `invalid:{jwt_token}`

- **Purpose:** Token blacklist
- **TTL:** Matches JWT token expiration time
- **Value:** `"true"`

### Redis (Product Service)

**Key Pattern:** Product cache keys (managed by Spring Cache)

- **Purpose:** Product data caching
- **TTL:** 600 seconds (10 minutes)

---

## Security Architecture

### Authentication Flow

1. **Registration:** User registers via Member Service (no authentication required)
2. **Login:** User logs in via API Gateway, which validates credentials with Member Service
3. **Token Generation:** API Gateway generates JWT token with user claims
4. **Token Storage:** Token returned in response body and HTTP-only cookie
5. **Request Authentication:** JWT Filter validates token on each request
6. **Token Blacklisting:** Logout adds token to Redis blacklist

### JWT Token Structure

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload (Claims):**
```json
{
  "sub": "username",           // Subject (username)
  "iss": "api-gateway",       // Issuer
  "aud": "online-marketplace", // Audience
  "iat": 1234567890,          // Issued at
  "exp": 1234654290,          // Expiration
  "email": "user@example.com", // Custom claim
  "active": true               // Custom claim
}
```

### Security Features

1. **Password Security:**
   - BCrypt hashing with salt
   - No plain text password storage
   - Password validation on login

2. **JWT Security:**
   - HS256 algorithm (HMAC with SHA-256)
   - Token expiration (24 hours)
   - Token blacklisting on logout
   - Signature validation on every request

3. **Cookie Security:**
   - HttpOnly: true (prevents XSS)
   - Secure: false (dev), true (production)
   - SameSite: Lax (CSRF protection)
   - Path: / (entire domain)

4. **Request Security:**
   - JWT validation on all authenticated endpoints
   - Token blacklist check in Redis
   - Username extraction and attribute injection

### Security Configuration

**API Gateway:**
- JWT Filter validates all requests except `/auth/login` and `/api/member/register`
- Token extraction from Authorization header or cookie
- Redis blacklist validation

**Member Service:**
- Spring Security with BCrypt password encoder
- Password validation on login
- Email uniqueness validation

---

## API Endpoints

### API Gateway Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/login` | User login, JWT generation | No |
| POST | `/auth/logout` | Logout, token invalidation | Yes |
| GET | `/auth/validate` | Validate JWT token | Yes |

### Member Service Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/member/register` | Register new user | No |
| POST | `/api/member/login` | Validate credentials | No |

### Product Service Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/product/createProduct` | Create product | Yes |
| GET | `/api/product/productDetail/productName/{name}` | Get by exact name | No |
| POST | `/api/product/searchProduct/productName/{name}` | Wildcard search | No |
| GET | `/api/product/searchProduct/category/{category}` | Search by category | No |
| GET | `/api/product/{id}` | Get product details | No |

### Cart Service Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/cart/{userId}/add` | Add item to cart | Yes |
| GET | `/api/cart/{userId}` | View cart | Yes |
| DELETE | `/api/cart/{userId}/remove/{productId}` | Remove item | Yes |

### Request/Response Examples

#### Login Request
```http
POST /auth/login
Content-Type: application/json

{
  "userName": "john_doe",
  "password": "password123"
}
```

#### Login Response
```json
{
  "status": "SUCCESS",
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userName": "john_doe"
  }
}
```

#### Add to Cart Request
```http
POST /api/cart/550e8400-e29b-41d4-a716-446655440000/add
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "productId": "MTA-000001",
  "quantity": 2
}
```

#### Add to Cart Response
```json
{
  "status": "SUCCESS",
  "message": "Item added to cart",
  "data": {
    "totalCartPrice": 2599.98,
    "items": [
      {
        "productId": "MTA-000001",
        "quantity": 2,
        "price": 2599.98
      }
    ]
  }
}
```

---

## Service Ports Summary

| Service | Port | Database | Cache |
|---------|------|----------|-------|
| API Gateway | 8080 | Redis | - |
| Member Service | 8083 | PostgreSQL | - |
| Product Service | 8081 | MongoDB | Redis |
| Cart Service | 8082 | MongoDB | - |

---

## Inter-Service Communication Summary

| From | To | Method | Purpose |
|------|-----|--------|---------|
| API Gateway | Member Service | WebClient | Login validation |
| API Gateway | Product Service | Gateway Routing | Product requests |
| API Gateway | Cart Service | Gateway Routing | Cart requests |
| Cart Service | Product Service | Feign Client | Product details |

---

## Conclusion

This Online Marketplace Application demonstrates a microservices architecture with:

- **Centralized Authentication:** API Gateway handles all authentication
- **Service Independence:** Each service has its own database
- **Reactive Gateway:** Spring Cloud Gateway for high performance
- **Inter-Service Communication:** Feign Client and WebClient for service-to-service calls
- **Security:** JWT tokens with blacklisting, BCrypt password hashing
- **Scalability:** Stateless services that can be scaled independently

The architecture follows microservices best practices with clear separation of concerns, independent deployment, and distributed data management.

