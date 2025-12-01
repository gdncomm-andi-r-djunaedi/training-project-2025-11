# Cart Service

User shopping cart backed by Redis.

## ✨ Features

- Add item to cart
- Remove item
- Clear cart
- Get cart contents
- Requires authenticated user (via gateway)
- Gateway injects `X-User-Id` header
- Optional product existence validation (via product-service)
- Swagger documentation

---

## 📚 Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/cart/{productId}?qty=1` | Add item |
| GET | `/api/cart` | View cart |
| DELETE | `/api/cart/{productId}` | Remove item |
| DELETE | `/api/cart` | Clear all |

---

## 🛠 Technologies

- Java 24
- Spring Boot 3.5.0
- Redis (Lettuce + StringRedisTemplate)
- Lombok 1.18.38
- SpringDoc OpenAPI

---

## 🌐 Swagger

http://localhost:8083/swagger-ui.html

---

## 🚀 Run

```bash
mvn spring-boot:run
```

---

## 📂 Structure

```
cart-service
 ├── controller/
 ├── service/
 ├── dto/
 ├── config/ (Redis)
 └── exception/ (if needed)
```
