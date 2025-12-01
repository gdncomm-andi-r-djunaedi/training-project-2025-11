# Training Microservices Project (Java 24 + Spring Boot 3.5.0)

This project is a microservices-based training application designed to demonstrate:

- Clean service boundaries  
- API Gateway (reverse proxy, routing, rate-limit, CORS, JWT validation)  
- Member authentication  
- Product catalog (MongoDB)  
- Shopping cart (Redis)  
- Shared common library (JWT + API error model)  
- SpringDoc OpenAPI documentation  
- Configurable security & routing  
- Clean controller/service architecture  

---

## 📌 Architecture Overview

```
[ client / browser ]
        |
        v
+--------------------+
|   API Gateway      |
|  - Reverse Proxy   |
|  - JWT validation  |
|  - Rate Limiting   |
|  - CORS rules      |
|  - Dynamic routes  |
+--------------------+
   |        |        |
   v        v        v
Member   Product   Cart
8081     8082      8083
```

---

## 📦 Modules

| Module | Description |
|--------|-------------|
| **api-gateway** | Central entrypoint. Reverse proxy, JWT verification, rate limiting, routing. |
| **member-service** | User registration, login, logout, JWT generation. MySQL/Postgres. |
| **product-service** | Product catalog with search + detail. MongoDB. |
| **cart-service** | User cart backed by Redis. |
| **common-lib** | Shared JWT service & API error model. |

---

## 🚀 How to Run

### 1️⃣ Build everything

```bash
mvn clean install
```

### 2️⃣ Run each service

```bash
cd member-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd cart-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

---

## 🔥 Swagger / API Documentation

| Service | Swagger URL |
|---------|-------------|
| Member | http://localhost:8081/swagger-ui.html |
| Product | http://localhost:8082/swagger-ui.html |
| Cart | http://localhost:8083/swagger-ui.html |

---

## 🔑 Auth Flow (JWT)

- `/api/members/register` → Public  
- `/api/members/login` → Returns `{ token }` + sets JWT cookie  
- Gateway forwards JWT and injects `X-User-Id` to authenticated routes  
- Cart-service requires authentication

---

## 📚 Technologies

- Java 24
- Spring Boot 3.5.0
- Spring Security 6
- Redis, MongoDB, MySQL/Postgres
- SpringDoc OpenAPI
- RestTemplate reverse proxy
- Lombok 1.18.38
- Maven multi-module project

---

## 🧼 Code Quality Patterns

- DTO layer (no entity exposure)
- Service layer separation
- Command Pattern for member registration
- Centralized exception handling per service
- Configurable gateway routing/security/CORS/rate-limit

---
