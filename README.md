# Training Project 2025-11

## Overview
This project is an online marketplace platform built with a microservices architecture. It allows customers to register, login, search for products, manage a shopping cart, and more.

## Architecture
The system consists of the following microservices:
- **API Gateway**: Entry point for all requests, handling authentication and routing.
- **Member Service**: Manages user registration and authentication.
- **Product Service**: Manages product catalog and search.
- **Cart Service**: Manages customer shopping carts.

## Technology Stack
- **Language**: Java
- **Framework**: Spring Boot
- **Databases**: PostgreSQL (Member), MongoDB (Product), Redis (Cart)
- **Build Tool**: Maven

## Security Implementation
The project implements robust security measures focusing on stateless authentication and secure data handling.

### Authentication Flow
1.  **Login**:
    -   Endpoint: `POST /auth/login`
    -   Credentials are validated by the Member Service.
    -   **JWT Generation**: Upon successful validation, the API Gateway generates a JSON Web Token (JWT) signed with a secure secret key.
    -   **Token Delivery**: The JWT is returned in the response body AND set as a secure HTTP cookie.
        -   Cookie Attributes: `HttpOnly`, `Secure`, `SameSite=Strict`, `Max-Age=1800` (30 mins).

2.  **Request Validation**:
    -   All protected requests must pass through the API Gateway.
    -   The Gateway validates the JWT from either the `Authorization: Bearer <token>` header OR the `jwt` cookie.
    -   Validated user ID are injected into downstream request headers (`X-User-Id`).

3.  **Logout**:
    -   Endpoint: `POST /auth/logout`
    -   The API Gateway invalidates the session by setting the `jwt` cookie's `Max-Age` to 0.
    -   Client-side should discard the token from memory.

### Password Security
-   **Hashing**: Customer passwords are hashed using `BCryptPasswordEncoder` (Spring Security) within the Member Service before storage.
-   **Validation**: Input validation is enforced using Spring's built-in validation library (Jakarta Bean Validation) to ensure password complexity and data integrity.

## API Endpoints

### Authentication
-   `POST /auth/register`: Register a new user.
-   `POST /auth/login`: Login and receive JWT.
-   `POST /auth/logout`: Logout and clear session cookie.

### Products
-   `GET /products`: List products (paginated).
-   `GET /products/{id}`: View product details.
-   `GET /products/search`: Search products (supports wildcards).

### Cart
-   `POST /cart`: Add item to cart.
-   `GET /cart`: View cart.
-   `DELETE /cart/{itemId}`: Remove item from cart.


## Running with Docker

You can run the entire system using Docker Compose.

```bash
# Start all services
docker-compose up --build -d
```

### Shortcuts
- **Windows**: `.\docker-run.bat`
- **Linux/Mac**: `./docker-run.sh`

## Requirements
For full requirements, refer to [Wiki](https://gdncomm.atlassian.net/wiki/spaces/GDNIT/pages/1787920458/2025+QA+to+BE+Conversion+Program+-+Final+Project).
