# Member Service

Handles registration, login, logout, and JWT generation.

## ✨ Features

- Register member (email, password, full name)
- Login with password hashing (BCrypt)
- Logout (clears cookie)
- JWT generation via `common-lib`
- Command pattern for registration
- Global exception handler (duplicate email, invalid credential, fallback)
- Swagger documentation

---

## 📚 Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/members/register` | Register user |
| POST | `/api/members/login` | Login + JWT cookie |
| POST | `/api/members/logout` | Clear JWT cookie |

---

## 🛠 Technologies

- Java 24
- Spring Boot 3.5.0
- Spring Data JPA (Postgres/MySQL)
- Spring Security (BCryptPasswordEncoder)
- Lombok 1.18.38
- SpringDoc OpenAPI

---

## 🌐 Swagger

http://localhost:8081/swagger-ui.html

---

## 🚀 Run

```bash
mvn spring-boot:run
```

---

## 📂 Structure

```
member-service
 ├── controller/
 ├── service/
 ├── command/
 ├── repository/
 ├── entity/
 ├── exception/
 └── model/ (DTOs)
```
