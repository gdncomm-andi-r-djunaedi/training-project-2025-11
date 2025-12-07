# Online Marketplace Platform

A microservices-based online marketplace backend built with Java Spring Boot. This project implements a comprehensive
API for a marketplace, featuring user authentication, product catalog management, and shopping cart functionality.

## Requirement Verification Report

| Category         | Requirement                 | Status | Implementation Details                                        |
|------------------|-----------------------------|--------|---------------------------------------------------------------|
| **Architecture** | Minimum 4 microservices     | ✅ Met  | `api-gateway`, `member`, `product`, `cart`                    |
|                  | API Gateway for AuthN/AuthZ | ✅ Met  | Implemented in `api-gateway` with JWT & Redis Blacklist       |
| **Tech Stack**   | Java & Spring               | ✅ Met  | Java 21, Spring Boot 3.4.1                                    |
|                  | PostgreSQL, MongoDB, Redis  | ✅ Met  | Member/Cart (Postgres), Product (Mongo), Gateway (Redis)      |
| **Auth**         | Register, Login, Logout     | ✅ Met  | `MemberService` & `Gateway`. BCrypt hashing used.             |
|                  | JWT & Session Validation    | ✅ Met  | JWT in secure HttpOnly cookie. `AuthFilter` validates tokens. |
|                  | Password Hashing            | ✅ Met  | Uses Spring Security `BCryptPasswordEncoder`                  |
| **Product**      | Search & View Details       | ✅ Met  | Wildcard search implemented in `ProductService`               |
|                  | Pagination                  | ✅ Met  | `Pageable` support in Search API                              |
|                  | Seeding (50k products)      | ✅ Met  | `data-seeder` module generates 50,000 products                |
| **Cart**         | Add/Remove Items            | ✅ Met  | Implemented in `CartService`                                  |
|                  | User specific cart          | ✅ Met  | Cart linked to User ID from JWT                               |
|                  | Unlimited Stock Assumption  | ✅ Met  | Stock check skipped as per requirements                       |
| **Quality**      | Unit & Integration Tests    | ✅ Met  | Tests present in all services                                 |
| **Design**       | Design Patterns             | ✅ Met  | Command Pattern, Builder Pattern, DTO Pattern used            |

## Architecture

The system consists of the following microservices:

1. **API Gateway** (Port 8080): Entry point, handles routing, authentication (JWT), and rate limiting. Uses **Redis**
   for token blacklisting on logout.
2. **Member Service** (Port 8081): Manages user registration and credentials. Uses **PostgreSQL**.
3. **Product Service** (Port 8082): Manages product catalog and search. Uses **MongoDB** for flexible schema and
   efficient document storage.
4. **Cart Service** (Port 8083): Manages shopping carts. Uses **PostgreSQL**.
5. **Data Seeder**: Utility module to populate databases with 5,000 members and 50,000 products.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.4.1, Spring Cloud Gateway, Spring Security
- **Databases:**
    - PostgreSQL (Member & Cart)
    - MongoDB (Product)
    - Redis (Gateway for blacklisting, Product for caching)
- **Authentication:** JWT (JSON Web Tokens) with HttpOnly Cookies
- **Testing:** JUnit 5, MockMvc, H2 (Test DB), Embedded MongoDB

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL (Port 5432)
- MongoDB (Port 27017)
- Redis (Port 6379)

## Database Setup

### PostgreSQL

Create the required databases:

```sql
CREATE DATABASE marketplace_member;
CREATE DATABASE marketplace_cart;
```

### MongoDB

MongoDB will auto-create the `marketplace_product` database on first connection.

## Running the Services

### 1. Build All Services

```bash
mvn clean package -DskipTests
```

### 2. Run Services (in separate terminals)

**API Gateway**

```bash
cd api-gateway
mvn spring-boot:run
```

**Member Service**

```bash
cd member
mvn spring-boot:run
```

**Product Service**

```bash
cd product
mvn spring-boot:run
```

**Cart Service**

```bash
cd cart
mvn spring-boot:run
```

### 3. Seed Data (Optional)

To populate the database with test data:

```bash
cd data-seeder
mvn spring-boot:run
```

## API Endpoints

All requests go through API Gateway at `http://localhost:8080`.

### Authentication

| Method | Endpoint               | Description                                  |
|--------|------------------------|----------------------------------------------|
| POST   | `/api/auth/login`      | Login and receive JWT cookie                 |
| POST   | `/api/auth/logout`     | Logout (invalidate cookie & blacklist token) |
| POST   | `/api/member/register` | Register new user                            |

**Login Example:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com", "password":"password123"}'
```

### Products

| Method | Endpoint                                        | Description                     |
|--------|-------------------------------------------------|---------------------------------|
| GET    | `/api/product/search?name=phone&page=0&size=10` | Search products with pagination |
| GET    | `/api/product/{id}`                             | Get product details             |

### Shopping Cart (Requires Login)

| Method | Endpoint                | Description              |
|--------|-------------------------|--------------------------|
| GET    | `/api/cart`             | View current user's cart |
| POST   | `/api/cart/add`         | Add item to cart         |
| DELETE | `/api/cart/{productId}` | Remove item from cart    |

**Add to Cart Example:**

```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "Content-Type: application/json" \
  -H "Cookie: auth_token=YOUR_JWT_TOKEN" \
  -d '{"productId":"PROD-123", "quantity": 1}'
```

## Project Structure

```
training-project-2025-11/
├── api-gateway/          # Gateway, Auth Filter, Redis Blacklist
├── member/               # User management (Postgres)
├── product/              # Product catalog (MongoDB)
├── cart/                 # Cart management (Postgres)
├── common-utils/         # Shared DTOs and utilities
└── data-seeder/          # Data generation tools
```
