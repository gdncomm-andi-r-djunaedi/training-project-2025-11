# Microservices API Documentation & Swagger URLs

## 🌐 Swagger UI Access

All microservices are configured with Swagger/OpenAPI documentation. Access them at the following URLs:

### Through API Gateway (Port 8084)
**Recommended:** Access all services through the API Gateway for proper authentication flow.

- **Base URL:** `http://localhost:8084`

### Direct Service Access

#### 1. Member Service (Port 8081)
- **Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8081/v3/api-docs`
- **Actuator Health:** `http://localhost:8081/actuator/health`

**Endpoints:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/members/{id}` - Get member by ID
- `PUT /api/members/{id}` - Update member
- `GET /api/members` - List all members

#### 2. Product Service (Port 8082)
- **Swagger UI:** `http://localhost:8082/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8082/v3/api-docs`
- **Actuator Health:** `http://localhost:8082/actuator/health`

**Endpoints:**
- `GET /api/products` - List products (with pagination)
- `GET /api/products?search={keyword}` - Search products (requires JWT)
- `GET /api/products/{id}` - Get product by ID (requires JWT)
- `POST /api/products` - Create product (requires JWT)
- `PUT /api/products/{id}` - Update product (requires JWT)

#### 3. Cart Service (Port 8083)
- **Swagger UI:** `http://localhost:8083/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8083/v3/api-docs`
- **Actuator Health:** `http://localhost:8083/actuator/health`

**Endpoints:**
- `GET /api/cart/{memberId}` - View cart (requires JWT)
- `POST /api/cart/{memberId}/items` - Add item to cart (requires JWT)
- `DELETE /api/cart/{memberId}/items/{productId}` - Remove item (requires JWT)

#### 4. API Gateway (Port 8080)
- **Actuator Health:** `http://localhost:8084/actuator/health`
- **Gateway Routes:** `http://localhost:8084/actuator/gateway/routes`

---

## 🔐 Authentication Flow

### 1. Register a New User
```bash
curl -X POST http://localhost:8084/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Response:**
```
User registered successfully!
```

### 2. Login to Get JWT Token
```bash
curl -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "testuser",
  "id": 5003
}
```

### 3. Use Token for Protected Endpoints
```bash
curl -X GET "http://localhost:8084/api/products?search=Marble" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

## 📝 Example API Calls

### Search Products
```bash
# Get token first
TOKEN=$(curl -s -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}' \
  | jq -r '.token')

# Search for products
curl -X GET "http://localhost:8084/api/products?search=Marble&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.'
```

### View Cart
```bash
curl -X GET "http://localhost:8084/api/cart/1" \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.'
```

### Add Item to Cart
```bash
curl -X POST "http://localhost:8084/api/cart/1/items?productId=PRODUCT_ID&quantity=2" \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.'
```

---

## 🗄️ Database Access

### PostgreSQL (Member & Cart Services)
```bash
# Connection details
Host: localhost
Port: 5432
Username: ecommerce_user
Password: ecommerce_pass

# Databases
- member_db
- cart_db
```

**Connect via psql:**
```bash
psql -h localhost -p 5432 -U ecommerce_user -d member_db
```

### MongoDB (Product Service)
```bash
# Connection details
Host: localhost
Port: 27017
Username: admin
Password: admin_pass
Database: product_db
```

**Connect via mongosh:**
```bash
mongosh "mongodb://admin:admin_pass@localhost:27017/product_db?authSource=admin"
```

### Redis (Caching)
```bash
# Connection details
Host: localhost
Port: 6379
```

**Connect via redis-cli:**
```bash
redis-cli -h localhost -p 6379
```

---

## 📊 Data Statistics

- **Members:** 5,000 seeded users
  - Default password: `password` (BCrypt encrypted)
  - Unique usernames and emails

- **Products:** 50,000 seeded products
  - Searchable by name and description
  - Unique SKUs (UUID format)
  - Realistic product names via Faker library

---

## 🔍 Testing the System

### Quick Verification Script
```bash
#!/bin/bash

# 1. Register
curl -X POST http://localhost:8084/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "email": "demo@test.com", "password": "demo123"}'

# 2. Login and get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "demo123"}' \
  | jq -r '.token')

echo "Token: $TOKEN"

# 3. Search products
curl -X GET "http://localhost:8080/api/products?search=Marble&size=5" \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.content[].name'

# 4. View cart
curl -X GET "http://localhost:8080/api/cart/1" \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.'
```

---

## 🚀 Service Management

### Start All Services
```bash
cd /Users/saputra.a.pratama/TrainingDev/Final
docker-compose up -d
```

### Stop All Services
```bash
docker-compose down
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f member-service
docker-compose logs -f product-service
docker-compose logs -f cart-service
docker-compose logs -f api-gateway
```

### Check Service Health
```bash
# Member Service
curl http://localhost:8081/actuator/health

# Product Service
curl http://localhost:8082/actuator/health

# Cart Service
curl http://localhost:8083/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health
```

---

## 🎯 Key Features Implemented

✅ **JWT Authentication** - Secure token-based auth
✅ **API Gateway Routing** - Centralized entry point
✅ **Product Search** - MongoDB text search (2,963 results for "Marble")
✅ **Redis Caching** - Performance optimization
✅ **Data Seeding** - 5K members + 50K products
✅ **Docker Compose** - Complete orchestration
✅ **Health Checks** - Actuator endpoints
✅ **Inter-Service Communication** - Cart ↔ Product validation

---

## 📚 Additional Resources

- **Implementation Plan:** `/Users/saputra.a.pratama/.gemini/antigravity/brain/093de05f-7bea-463a-bc06-6c695c5b18bd/implementation_plan.md`
- **Walkthrough:** `/Users/saputra.a.pratama/.gemini/antigravity/brain/093de05f-7bea-463a-bc06-6c695c5b18bd/walkthrough.md`
- **Task List:** `/Users/saputra.a.pratama/.gemini/antigravity/brain/093de05f-7bea-463a-bc06-6c695c5b18bd/task.md`
