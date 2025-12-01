# Common Library

Shared components reused by multiple services.

## ✨ Contains

### 1. JwtService

Utility to generate and validate JWT tokens:

```java
String jwt = jwtService.generateToken(userId, claims, ttlSeconds);
String subject = jwtService.validateAndGetSubject(jwt);
```

### 2. ApiError

Standard error response for all services:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Email already registered",
  "path": "/api/members/register"
}
```

---

## 🛠 Technologies

- Java 24
- No Spring dependency required (pure utility + model)

---

## 🚀 Build

Must be installed before other modules:

```bash
mvn -pl common-lib clean install
```
