  # Online Marketplace Platform

A microservices-based online marketplace backend built with Java Spring Boot. This project implements a comprehensive
API for a marketplace, featuring user authentication, product catalog management, and shopping cart functionality.

---

## 📋 Requirements Verification Report

### ✅ Business Requirements

| Requirement | Status | Evidence |
|-------------|--------|----------|
| Build online marketplace platform | ✅ **Met** | Complete e-commerce backend with registration, products, and cart |

### ✅ Use Case API Requirements

| Use Case | Requirement | Status | Implementation |
|----------|-------------|--------|----------------|
| **Authentication** | Customer register/login | ✅ **Met** | `MemberController.register()`, `AuthController.login()` |
| | Password hashing (Spring Security) | ✅ **Met** | `BCryptPasswordEncoder` in [SecurityConfig.java](file:///d:/code/blibli/training/project/training-project-2025-11/member/src/main/java/com/marketplace/member/config/SecurityConfig.java) |
| | Password validation (Spring Security) | ✅ **Met** | `PasswordEncoder.matches()` in [ValidateCredentialsCommandImpl.java](file:///d:/code/blibli/training/project/training-project-2025-11/member/src/main/java/com/marketplace/member/command/impl/ValidateCredentialsCommandImpl.java) |
| | JWT token authentication | ✅ **Met** | JWT generation in [LoginCommandImpl.java](file:///d:/code/blibli/training/project/training-project-2025-11/api-gateway/src/main/java/com/marketplace/gateway/command/impl/LoginCommandImpl.java) |
| **Product** | Search products (paginated) | ✅ **Met** | `Pageable` support in [ProductController.java](file:///d:/code/blibli/training/project/training-project-2025-11/product/src/main/java/com/marketplace/product/controller/ProductController.java) |
| | Wildcard search | ✅ **Met** | `findByNameContainingIgnoreCase()` in [ProductRepository.java](file:///d:/code/blibli/training/project/training-project-2025-11/product/src/main/java/com/marketplace/product/repository/ProductRepository.java) |
| | View product list & details | ✅ **Met** | `searchProducts()` and `getProductById()` endpoints |
| **Cart** | Add product to cart (logged-in) | ✅ **Met** | [AddToCartCommandImpl.java](file:///d:/code/blibli/training/project/training-project-2025-11/cart/src/main/java/com/marketplace/cart/command/impl/AddToCartCommandImpl.java) - requires `X-User-Id` header |
| | View shopping cart | ✅ **Met** | [GetCartCommandImpl.java](file:///d:/code/blibli/training/project/training-project-2025-11/cart/src/main/java/com/marketplace/cart/command/impl/GetCartCommandImpl.java) |
| | Delete from cart | ✅ **Met** | [RemoveFromCartCommandImpl.java](file:///d:/code/blibli/training/project/training-project-2025-11/cart/src/main/java/com/marketplace/cart/command/impl/RemoveFromCartCommandImpl.java) |
| | No inventory check | ✅ **Met** | Stock check skipped - unlimited assumption |
| **Session** | JWT Cookie OR Header validation | ✅ **Met** | [AuthFilter.java](file:///d:/code/blibli/training/project/training-project-2025-11/api-gateway/src/main/java/com/marketplace/gateway/filter/AuthFilter.java) checks both Cookie and Authorization header |
| **Logout** | Invalidate JWT token/cookie | ✅ **Met** | Cookie `Max-Age=0` + Redis blacklist in [TokenBlacklistService.java](file:///d:/code/blibli/training/project/training-project-2025-11/api-gateway/src/main/java/com/marketplace/gateway/service/TokenBlacklistService.java) |

### ✅ Technical Requirements

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Minimum 4 microservices | ✅ **Met** | `api-gateway`, `member`, `product`, `cart` |
| API-only (no UI) | ✅ **Met** | REST API endpoints only |
| API Gateway for AuthN/AuthZ | ✅ **Met** | Spring Cloud Gateway with JWT filter |
| Java + Spring | ✅ **Met** | Java 21, Spring Boot 3.4.1 |
| PostgreSQL | ✅ **Met** | Member & Cart databases |
| MongoDB | ✅ **Met** | Product catalog |
| Redis | ✅ **Met** | Token blacklisting in Gateway |
| Unit & Integration Tests | ✅ **Met** | 10+ test files across all services |
| 5,000 members seeded | ✅ **Met** | [DataSeederRunner.java](file:///d:/code/blibli/training/project/training-project-2025-11/data-seeder/src/main/java/com/marketplace/seeder/runner/DataSeederRunner.java) seeds 5,000 members |
| 50,000 products seeded | ✅ **Met** | [DataSeederRunner.java](file:///d:/code/blibli/training/project/training-project-2025-11/data-seeder/src/main/java/com/marketplace/seeder/runner/DataSeederRunner.java) seeds 50,000 products |

### ✅ Security Requirements

| Security Point | Status | Implementation |
|----------------|--------|----------------|
| Password hashing (BCrypt) | ✅ **Met** | Member Service only |
| JWT creation | ✅ **Met** | API Gateway with `JwtUtil.generateToken()` |
| JWT validation | ✅ **Met** | API Gateway with `JwtUtil.validateToken()` |
| JWT in response body AND Set-Cookie | ✅ **Met** | [AuthController.login()](file:///d:/code/blibli/training/project/training-project-2025-11/api-gateway/src/main/java/com/marketplace/gateway/controller/AuthController.java) |
| Cookie + Header extraction | ✅ **Met** | `AuthFilter.extractToken()` checks both |
| HttpOnly, Secure, SameSite=Strict | ✅ **Met** | [CookieUtil.java](file:///d:/code/blibli/training/project/training-project-2025-11/api-gateway/src/main/java/com/marketplace/gateway/util/CookieUtil.java) |
| Header format: `Bearer <JWT>` | ✅ **Met** | Gateway filter validates Bearer prefix |
| JWT payload (user_id, roles, exp, iat) | ✅ **Met** | Implemented in `JwtUtil` |
| Services trust Gateway | ✅ **Met** | Gateway forwards `X-User-Id`, `X-User-Email` |
| Cookie logout (Max-Age=0) | ✅ **Met** | `CookieUtil.createLogoutCookie()` |
| Token blacklist on logout | ✅ **Met** | Redis-based `TokenBlacklistService` |
| Stateless auth | ✅ **Met** | No server-side session storage |

### ✅ Evaluation Criteria

| Criteria | Status | Notes |
|----------|--------|-------|
| Functional completeness | ✅ **Met** | All use cases implemented |
| System design (API/DB) | ✅ **Met** | RESTful API, proper DB separation |
| Code cleanliness | ✅ **Met** | Command Pattern, layered architecture |
| Security | ✅ **Met** | JWT, BCrypt, HTTPS cookies |
| Performance | ✅ **Met** | Redis caching, pagination |
| Testability | ✅ **Met** | Unit & Integration tests |

### ⭐ Extra Challenges (Optional)

| Challenge | Status | Implementation |
|-----------|--------|----------------|
| Docker/Kubernetes | ✅ **Met** | `Dockerfile`, `docker-compose.yml` |
| ElasticSearch | ✅ **Met** | [ProductSearchRepository.java](file:///d:/code/blibli/training/project/training-project-2025-11/product/src/main/java/com/marketplace/product/repository/ProductSearchRepository.java) |
| Design Patterns | ✅ **Met** | Command Pattern, Builder, DTO Pattern |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         API Gateway (8080)                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────────┐  │
│  │ AuthFilter  │  │ JwtAuth     │  │ TokenBlacklistService       │  │
│  │ (Global)    │  │ Filter      │  │ (Redis)                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
         │                  │                      │
         ▼                  ▼                      ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ Member Service  │ │ Product Service │ │  Cart Service   │
│     (8081)      │ │     (8082)      │ │     (8083)      │
├─────────────────┤ ├─────────────────┤ ├─────────────────┤
│  PostgreSQL     │ │  MongoDB        │ │  PostgreSQL     │
│  (members)      │ │  (products)     │ │  (carts)        │
│                 │ │  ElasticSearch  │ │                 │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

## 🧪 Test Coverage

| Service | Test Files | Type |
|---------|------------|------|
| API Gateway | `LoginCommandImplTest`, `AuthControllerTest`, `AuthFilterTest`, `JwtAuthenticationFilterTest`, `TokenBlacklistServiceTest` | Unit |
| Member | `MemberControllerIntegrationTest`, `PasswordValidatorTest` | Integration, Unit |
| Product | `ProductControllerIntegrationTest` | Integration |
| Cart | `CartControllerIntegrationTest` | Integration |
| Common | `JwtUtilTest` | Unit |

---

## 🛠️ Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.4.1, Spring Cloud Gateway, Spring Security
- **Databases:**
    - PostgreSQL (Member & Cart)
    - MongoDB (Product)
    - Redis (Gateway token blacklisting)
    - ElasticSearch (Product search)
- **Authentication:** JWT with HttpOnly Cookies
- **Testing:** JUnit 5, MockMvc, H2, Embedded MongoDB

## 📋 Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL (Port 5432)
- MongoDB (Port 27017)
- Redis (Port 6379)
- ElasticSearch (Port 9200) - optional

## 🗃️ Database Setup

### PostgreSQL

```sql
CREATE DATABASE marketplace_member;
CREATE DATABASE marketplace_cart;
```

### MongoDB

Auto-creates `marketplace_product` database on first connection.

## 🚀 Running the Services

### Option 1: Docker Compose (Recommended)

```bash
# Start all databases
docker-compose -f docker-compose.db.yml up -d

# Build and run all services
docker-compose up --build
```

### Option 2: Manual

```bash
# Build all
mvn clean package -DskipTests

# Run each service in separate terminals
cd api-gateway && mvn spring-boot:run
cd member && mvn spring-boot:run
cd product && mvn spring-boot:run
cd cart && mvn spring-boot:run

# Seed data (optional)
cd data-seeder && mvn spring-boot:run
```

## 📡 API Endpoints

All requests go through API Gateway at `http://localhost:8080`.

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/member/register` | Register new user |
| POST | `/api/auth/login` | Login (returns JWT cookie) |
| POST | `/api/auth/logout` | Logout (invalidates cookie + blacklists token) |

### Products (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/product/search?name=phone&page=0&size=10` | Search products |
| GET | `/api/product/{id}` | Get product details |

### Cart (Requires Login)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cart` | View cart |
| POST | `/api/cart/add` | Add item |
| DELETE | `/api/cart/{productId}` | Remove item |

## 📁 Project Structure

```
training-project-2025-11/
├── api-gateway/        # Gateway, Auth, JWT, Redis blacklist
├── member/             # User management (PostgreSQL)
├── product/            # Product catalog (MongoDB, ElasticSearch)
├── cart/               # Shopping cart (PostgreSQL)
├── common-utils/       # Shared DTOs, utilities, Command pattern
├── data-seeder/        # Seeds 5,000 members + 50,000 products
├── docker/             # Docker configurations
├── docker-compose.yml  # Full stack orchestration
└── requirements/       # Project requirements documentation
```

---

## 📄 License

MIT License
