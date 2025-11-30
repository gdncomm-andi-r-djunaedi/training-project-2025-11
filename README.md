# Training Project 2025-11 - Microservices E-Commerce Platform

## Overview
This is a microservices-based online marketplace platform built with Spring Boot, implementing authentication, product catalog, and shopping cart functionality.

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

**PostgreSQL:**
```bash
psql -U postgres -f member/setup-db.sql
psql -U postgres -f product/setup-db.sql
```

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

### 4. Seed Product Data

After Product service starts:
```bash
psql -U product_user -d product -f product/setup-db.sql
```

### 5. Test the API

See [`walkthrough.md`](file:///C:/Users/User/.gemini/antigravity/brain/2f22b458-4575-42cb-92a4-b2c0229bbd0f/walkthrough.md) for complete testing guide.

## API Endpoints

### Authentication (via Gateway :8080)
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login (returns JWT cookie)
- `POST /auth/logout` - Logout (clears cookie)

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
✅ Logout functionality  
✅ Product search with wildcard support  
✅ Pagination for product listing  
✅ Shopping cart (add, view, remove)  
✅ Authorization via JWT validation in API Gateway  
✅ User ID propagation via `X-User-Id` header  

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
