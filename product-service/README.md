# Product Service

Provides product search, filtering and product details.

## ✨ Features

- MongoDB-backed product catalog
- Search by name with pagination
- DTO response model (summary & detail)
- Global exception handler (ProductNotFound)
- Demo data seeder (50,000 products)
- Swagger documentation

---

## 📚 Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/products` | Paginated search (`page`, `size`, `q`) |
| GET | `/api/products/{id}` | Product detail |

---

## 🛠 Technologies

- Java 24
- Spring Boot 3.5.0
- Spring Data MongoDB
- Lombok 1.18.38
- SpringDoc OpenAPI

---

## 🌐 Swagger

http://localhost:8082/swagger-ui.html

---

## 🚀 Run

```bash
mvn spring-boot:run
```

---

## 📂 Structure

```
product-service
 ├── controller/
 ├── service/
 ├── dto/
 ├── entity/
 ├── model/ (enums like ProductTag)
 ├── repository/
 └── exception/
```
