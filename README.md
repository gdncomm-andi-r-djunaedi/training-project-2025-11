# Training Project 2025-11 - Microservices E-Commerce Platform

## Overview
This is a microservices-based online marketplace platform built with Spring Boot, implementing JWT-based authentication, product catalog, caching, and shopping cart functionality.

## Architecture
- **API Gateway** (Port 8080) - Entry point, JWT validation, request routing
- **Member Service** (Port 8081) - User registration, login, logout (PostgreSQL)
- **Product Service** (Port 8082) - Product catalog with search (PostgreSQL)
- **Cart Service** (Port 8083) - Shopping cart management (MongoDB)

## Tech Stack
- **Java 21** with Spring Boot 3.5.8
- **Databases**: PostgreSQL (Member, Product), MongoDB (Cart)
- **Security**: JWT with HttpOnly cookies
- **Build Tool**: Maven

## Quick Start

### 1. Prerequisites
- Java 21+
- PostgreSQL 12+
- MongoDB 4.4+
- Maven 3.8+

### 2. Database Setup

**PostgreSQL (members + products):**
```bash
psql -U postgres -d member -f member/setup-db.sql
psql -U postgres -d product -f product/setup-db.sql
```
The scripts will generate 5,000 members (with BCrypt hashed passwords) and 50,000 products using `gen_random_uuid()`. Ensure the `pgcrypto` extension is enabled on both databases (`CREATE EXTENSION IF NOT EXISTS "pgcrypto";`).

**MongoDB:**
MongoDB will auto-create the database on first connection.

### 3. Start Services

```bash
# In 4 separate terminals:
cd member && ./mvnw spring-boot:run
cd product && ./mvnw spring-boot:run
cd cart && ./mvnw spring-boot:run
cd api-gateway && ./mvnw spring-boot:run
```

### 4. Seed Data (optional re-run)
If you need to refresh data without restarting the apps:
```bash
psql -U member_user -d member -f member/setup-db.sql
psql -U product_user -d product -f product/setup-db.sql
```

### 5. Test the API

See [`walkthrough.md`](file:///C:/Users/User/.gemini/antigravity/brain/2f22b458-4575-42cb-92a4-b2c0229bbd0f/walkthrough.md) for complete testing guide.

## API Endpoints

### Authentication (via Gateway :8080)
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login (returns JWT cookie + token body)
- `POST /auth/logout` - Logout (clears cookie + blacklists token in Redis)

### Products (via Gateway :8080)
- `GET /products?query={search}&page={n}&size={n}` - Search products
- `GET /products/{id}` - Get product details

### Cart (via Gateway :8080) - Requires Authentication
- `GET /cart` - View cart
- `POST /cart` - Add item to cart
- `DELETE /cart/{productId}` - Remove item

## Features Implemented
✅ User registration with password hashing (BCrypt)  
✅ JWT-based authentication with HttpOnly cookies  
✅ Logout with Redis-backed token blacklist  
✅ Product search with wildcard support  
✅ Pagination for product listing  
✅ Shopping cart (add, view, remove)  
✅ Authorization via JWT validation in API Gateway  
✅ User ID propagation via `X-User-Id` header  
✅ Redis cache for product detail lookups  

## Project Structure
```
training-project-2025-11/
├── api-gateway/          # API Gateway with JWT filter
├── member/               # Member service (Auth)
├── product/              # Product service (Catalog)
├── cart/                 # Cart service (Shopping)
└── README.md
```

## Development

### Build All Services
```bash
./mvnw clean install
```

### Run Tests
```bash
./mvnw test
```

## License
MIT
