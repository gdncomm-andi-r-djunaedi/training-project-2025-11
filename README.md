Modules:
- common
- api-gateway
- member-service (Postgres, BCrypt, JWT)
- product-service (Postgres, search + pagination)
- cart-service (MongoDB, JWT auth, calls member + product)

## 1. Start infra

```bash
docker compose up -d
```

## 2. Build all modules

```bash
mvn clean install
```

## 3. Run services

```bash
# separate terminals
mvn -pl member-service spring-boot:run
mvn -pl product-service spring-boot:run
mvn -pl cart-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

Ports:
- api-gateway: 8080
- member-service: 8081
- product-service: 8082
- cart-service: 8083

## 4. Swagger

- Member:  http://localhost:8081/swagger-ui/index.html
- Product: http://localhost:8082/swagger-ui/index.html
- Cart:    http://localhost:8083/swagger-ui/index.html

## 5. Flow via Swagger (direct services)

### 5.1 Register user (member-service)

POST http://localhost:8081/internal/members/register

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "Password123"
}
```

Copy `data.id` (userId) and also later you can login:

### 5.2 Login to get JWT

POST http://localhost:8081/internal/members/login

```json
{
  "email": "alice@example.com",
  "password": "Password123"
}
```

Response `data.token` is a JWT.

### 5.3 Create product

POST http://localhost:8082/internal/products

```json
{
  "id": "p-100",
  "name": "Sample Phone",
  "description": "Nice phone",
  "price": 499.99
}
```

### 5.4 Add to cart

Use cart-service Swagger: POST /internal/cart

Header:
- `Authorization: Bearer <JWT_FROM_LOGIN>`

Body:

```json
{
  "productId": "p-100",
  "quantity": 2
}
```

Cart service will:
- Parse JWT to get userId
- Call member-service /internal/members/{id}/exists
- Call product-service /internal/products/{id}
- Store cart document in Mongo

### 5.5 Get cart

GET /internal/cart with same `Authorization: Bearer <JWT>` header.

## 6. Flow via Gateway (optional)

You can also test through api-gateway:

- Register: POST http://localhost:8080/api/auth/register
- Login:    POST http://localhost:8080/api/auth/login
- List products: GET http://localhost:8080/api/products
- Add to cart: POST http://localhost:8080/api/cart (Authorization: Bearer <JWT>)
- Get cart: GET http://localhost:8080/api/cart (Authorization: Bearer <JWT>)
