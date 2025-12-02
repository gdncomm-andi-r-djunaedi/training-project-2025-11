# Training Project 2025-11 - Online Marketplace API

This project is a microservices-based online marketplace API built with Java Spring Boot and Maven.

## Architecture

The project consists of the following microservices:

1.  **API Gateway** (Port 8080): Entry point for all requests. Handles Authentication and Routing.
2.  **Member Service** (Port 8081): Handles User Registration, Login, and Logout. Uses PostgreSQL and Redis.
3.  **Product Service** (Port 8082): Handles Product Search and Details. Uses MongoDB.
4.  **Cart Service** (Port 8083): Handles Shopping Cart operations. Uses PostgreSQL.

## Prerequisites

-   Java 17
-   Maven
-   PostgreSQL (Running on localhost:5432)
-   MongoDB (Running on localhost:27017)
-   Redis (Running on localhost:6379)

## Database Setup

Ensure you have the following databases created in PostgreSQL:
-   `member_db`
-   `cart_db`

MongoDB will automatically create the `product_db` database and `products` collection upon first write.

## Running the Services

You can run each service using the following command in their respective directories:

```bash
mvn spring-boot:run
```

Start them in this order:
1.  API Gateway
2.  Member Service
3.  Product Service
4.  Cart Service

## API Endpoints

All endpoints are accessed via the API Gateway at `http://localhost:8080`.

### Member Service
-   **Register**: `POST /api/member/register`
    ```json
    {
      "username": "john_doe",
      "password": "password123",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe"
    }
    ```
-   **Login**: `POST /api/member/login`
    ```json
    {
      "username": "john_doe",
      "password": "password123"
    }
    ```
    *Returns a JWT Token.*
-   **Logout**: `POST /api/member/logout`
    *Headers*: `Authorization: Bearer <token>`

### Product Service
-   **Search Products**: `GET /api/product/search?query=phone&page=0&size=10`
    *Headers*: `Authorization: Bearer <token>`
-   **Get Product by ID**: `GET /api/product/{id}`
    *Headers*: `Authorization: Bearer <token>`
-   **Create Product** (Internal): `POST /api/product`
    *Headers*: `Authorization: Bearer <token>`
    ```json
    {
      "name": "iPhone 15",
      "description": "Latest iPhone",
      "price": 999.99,
      "imageUrl": "http://example.com/iphone.jpg",
      "category": "Electronics"
    }
    ```

### Cart Service
-   **Add to Cart**: `POST /api/cart/add`
    *Headers*: `Authorization: Bearer <token>`
    ```json
    {
      "memberId": 1,
      "productId": "product_id_here",
      "quantity": 1
    }
    ```
-   **Get Cart**: `GET /api/cart/{memberId}`
    *Headers*: `Authorization: Bearer <token>`
-   **Remove from Cart**: `DELETE /api/cart/{memberId}/remove/{productId}`
    *Headers*: `Authorization: Bearer <token>`

## Authentication

-   The API Gateway validates the JWT token for all secured endpoints.
-   Login and Register endpoints are public.
-   Logout invalidates the token by adding it to a Redis blacklist.
