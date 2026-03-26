# Marketplace Platform API Documentation

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Base URLs](#base-urls)
4. [Authentication](#authentication)
5. [API Endpoints](#api-endpoints)
   - [Member Service APIs](#member-service-apis)
   - [Product Service APIs](#product-service-apis)
   - [Cart Service APIs](#cart-service-apis)
6. [Error Responses](#error-responses)
7. [Database Schema](#database-schema)

---

## Overview

### Goal

Building an online marketplace platform with the following capabilities:

- Customers can register and login
- Customers can search and browse products
- Customers can add products to cart
- System can handle 5,000+ customers and 50,000+ products

### Important Features

- **Registration and Login** - New user should be able to register
- **Product Search** - Ability to search products with pagination
- **Cart Functionality** - Ability to add/remove products from cart
- **Authentication** - Using JWT tokens with headers

---

## Architecture

### System Components

#### API Gateway (Port: 8092)
- **Purpose**: Authenticates and routes requests to the correct service
- **Main Entry Point**: Receives all requests from customers
- **Key Functions**:
  - Creates login tokens (JWT) when user logs in successfully
  - Validates tokens to check if user is logged in
  - Routes request to the correct service
- **Key Components**: `JwtAuthenticationFilter`, `JwtUtil`, `LoginResponseFilter`, Spring Cloud Gateway Routes

#### Member Service (Port: 8091)
- **Purpose**: Handling customer accounts
- **Key Functions**:
  - Registers new customers
  - Handles login (verifies email and password)
  - Stores user information
  - Hashes passwords using BCrypt
- **Key Components**: `MemberController`, `MemberService`, `MemberRepository`
- **Database**: PostgreSQL

#### Product Service (Port: 8084)
- **Purpose**: Handling all product-related flows
- **Key Functions**:
  - Stores product information (Name, description, price, category, etc.)
  - Searches products (Finds products by name/description)
  - Lists products (Shows products page by page with pagination)
  - Shows product details (Returns full information about a product)
- **Key Components**: `ProductController`, `ProductService`, `ProductRepository`
- **Database**: MongoDB
- **Cache**: Redis (Product search results: 5-10 minutes, Product details: 30 minutes)

#### Cart Service (Port: 8098)
- **Purpose**: Handling add/remove cart items
- **Key Functions**:
  - Adds products to cart (When user clicks "Add to Cart")
  - Shows cart contents (Displays all items in user's cart)
  - Removes products from cart (When user removes an item)
  - Calculates totals (Sum of all items in cart)
- **Key Components**: `CartController`, `CartService`, `CartRepository`
- **Database**: MongoDB
- **Cache**: Redis (User cart information: 30 minutes)

---

## Base URLs

| Service | Base URL | Port |
|---------|----------|------|
| API Gateway | `http://localhost:8092` | 8092 |
| Member Service | `http://localhost:8091` | 8091 |
| Product Service | `http://localhost:8084` | 8084 |
| Cart Service | `http://localhost:8098` | 8098 |

**Note**: All requests should be made through the API Gateway unless testing services directly.

---

## Authentication

### JWT Token Structure

JWT tokens are JSON web tokens used for authentication. Example token structure:

```json
{
  "memberId": 123,
  "email": "john@example.com",
  "exp": 1234571490
}
```

### How JWT Works

1. **User logs in** → Gets JWT token in response
2. **Token stored** → Client stores token (in memory, localStorage, or cookie)
3. **Protected requests** → Client includes token in `Authorization` header
4. **Gateway validates** → Gateway validates token and extracts `memberId`
5. **Request forwarded** → Gateway adds `X-Member-Id` header and forwards to service

### Sending Tokens

**Header-based (Recommended)**:
```
Authorization: Bearer <jwt-token>
```

The gateway automatically extracts `memberId` from the token and adds it as `X-Member-Id` header to downstream services.

### Token Expiration

- Default expiration: 60 seconds (configurable)
- Production recommendation: 24 hours (86400000 milliseconds)

---

## API Endpoints

### Standard Response Format

All API responses follow this structure:

```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
  }
}
```

**Error Response**:
```json
{
  "success": false,
  "errorMessage": "Error description",
  "errorCode": "ERROR_CODE",
  "value": null
}
```

---

## Member Service APIs

### 1. Register New User

**Endpoint**: `POST /api/members/register`

**Description**: Creates a new user account in the system.

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "password123",
  "name": "John Doe",
  "phone": "+1234567890"
}
```

**Request Fields**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| email | String | Yes | Valid email format | User's email address |
| password | String | Yes | Minimum 6 characters | User's password |
| name | String | No | - | User's full name |
| phone | String | No | - | Contact number |

**Response**: `200 OK`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "memberId": 1,
    "email": "john@example.com",
    "name": "John Doe",
    "phone": "+1234567890"
  }
}
```

**Error Responses**:

- **400 Bad Request** - Invalid Input
```json
{
  "success": false,
  "errorMessage": "Validation failed: Email must be valid",
  "errorCode": "VALIDATION_ERROR",
  "value": null
}
```

- **400 Bad Request** - Duplicate Email
```json
{
  "success": false,
  "errorMessage": "Email already exists: john@example.com",
  "errorCode": "BAD_REQUEST",
  "value": null
}
```

---

### 2. Login

**Endpoint**: `POST /api/members/login`

**Description**: Authenticates user and returns JWT token.

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Request Fields**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| email | String | Yes | Valid email format | User's email address |
| password | String | Yes | - | User's password |

**Response**: `200 OK`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "member": {
      "memberId": 1,
      "email": "john@example.com",
      "name": "John Doe",
      "phone": "+1234567890"
    }
  }
}
```

**Note**: The JWT token is generated by the gateway service and added to the response automatically.

**Error Responses**:

- **400 Bad Request** - Invalid Credentials
```json
{
  "success": false,
  "errorMessage": "Invalid email or password",
  "errorCode": "BAD_REQUEST",
  "value": null
}
```

- **400 Bad Request** - Validation Error
```json
{
  "success": false,
  "errorMessage": "Validation failed: Email must be valid",
  "errorCode": "VALIDATION_ERROR",
  "value": null
}
```

---

### 3. Logout

**Endpoint**: `POST /api/members/logout`

**Description**: Logs out the current user.

**Request Headers**:
```
Content-Type: application/json
```

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | Member ID to logout |

**Request Example**:
```
POST /api/members/logout?memberId=1
```

**Response**: `200 OK`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": "Logout successful"
}
```

**Error Responses**:

- **400 Bad Request** - Member Not Found
```json
{
  "success": false,
  "errorMessage": "Member not found with id: 999",
  "errorCode": "BAD_REQUEST",
  "value": null
}
```

---

## Product Service APIs

### 4. Search Products

**Endpoint**: `GET /api/products/search`

**Description**: Searches products by query string with pagination support.

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| searchTerm | String | No | - | Search query (supports wildcards) |
| category | String | No | - | Filter by category |
| page | Integer | No | 0 | Page number (starts from 0) |
| size | Integer | No | 10 | Items per page |

**Request Example**:
```
GET /api/products/search?searchTerm=laptop&page=0&size=20&category=Electronics
```

**Response**: `200 OK`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "content": [
      {
        "productId": "PROD-001",
        "name": "MacBook Pro 16",
        "description": "Latest MacBook with M3 chip",
        "price": 2499.99,
        "category": "Electronics",
        "brand": "Apple",
        "imageUrl": "https://example.com/macbook.jpg",
        "attributes": {
          "color": "Space Gray",
          "storage": "512GB",
          "ram": "16GB"
        },
        "isActive": true
      },
      {
        "productId": "PROD-002",
        "name": "Dell XPS 15",
        "description": "High-performance laptop",
        "price": 1799.99,
        "category": "Electronics",
        "brand": "Dell",
        "imageUrl": "https://example.com/dell.jpg",
        "attributes": {
          "color": "Silver",
          "storage": "256GB",
          "ram": "8GB"
        },
        "isActive": true
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

**Response**: `200 OK` (No Results)
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

**Note**: This endpoint is public and does not require authentication.

---

### 5. List Products (Paginated)

**Endpoint**: `GET /api/products/listing`

**Description**: Retrieves paginated list of all active products, optionally filtered by category.

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| category | String | No | - | Filter by category |
| page | Integer | No | 0 | Page number (starts from 0) |
| size | Integer | No | 10 | Items per page |

**Request Example**:
```
GET /api/products/listing?page=0&size=20&category=Electronics
```

**Response**: `200 OK`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "content": [
      {
        "productId": "PROD-001",
        "name": "iPhone 15",
        "description": "Latest iPhone with advanced features",
        "price": 999.99,
        "category": "Electronics",
        "brand": "Apple",
        "imageUrl": "https://example.com/iphone.jpg",
        "attributes": {
          "color": "Natural Titanium",
          "storage": "128GB",
          "ram": "6GB"
        },
        "isActive": true
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50000,
    "totalPages": 2500
  }
}
```

**Note**: This endpoint is public and does not require authentication.

---

### 6. Get Product Details

**Endpoint**: `GET /api/products/detail/{productId}`

**Description**: Retrieves detailed information about a specific product.

**Path Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| productId | String | Yes | Unique product identifier |

**Request Example**:
```
GET /api/products/detail/PROD-001
```

**Response**: `200 OK`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "productId": "PROD-001",
    "name": "iPhone 15",
    "description": "Latest iPhone with advanced features, A17 Pro chip, and ProRAW photography",
    "category": "Electronics",
    "price": 999.99,
    "brand": "Apple",
    "imageUrl": "https://example.com/iphone.jpg",
    "attributes": {
      "color": "Natural Titanium",
      "storage": "128GB",
      "ram": "6GB",
      "screenSize": "6.1 inches",
      "battery": "3349 mAh"
    },
    "isActive": true
  }
}
```

**Error Responses**:

- **400 Bad Request** - Product Not Found
```json
{
  "success": false,
  "errorMessage": "Product not found with id: PROD-99999",
  "errorCode": "BAD_REQUEST",
  "value": null
}
```

**Note**: This endpoint is public and does not require authentication.

---

### 7. Create Product (Admin)

**Endpoint**: `POST /api/products/create`

**Description**: Creates a new product in the system.

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "productId": "PROD-001",
  "name": "iPhone 15",
  "description": "Latest iPhone with advanced features",
  "category": "Electronics",
  "price": 999.99,
  "brand": "Apple",
  "imageUrl": "https://example.com/iphone.jpg",
  "attributes": {
    "color": "Natural Titanium",
    "storage": "128GB",
    "ram": "6GB"
  },
  "isActive": true
}
```

**Response**: `201 Created`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "productId": "PROD-001",
    "name": "iPhone 15",
    "description": "Latest iPhone with advanced features",
    "category": "Electronics",
    "price": 999.99,
    "brand": "Apple",
    "imageUrl": "https://example.com/iphone.jpg",
    "attributes": {
      "color": "Natural Titanium",
      "storage": "128GB",
      "ram": "6GB"
    },
    "isActive": true
  }
}
```

---

### 8. Delete Product (Admin)

**Endpoint**: `DELETE /api/products/{productId}`

**Description**: Deletes a product by productId and evicts from cache.

**Path Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| productId | String | Yes | Unique product identifier |

**Request Example**:
```
DELETE /api/products/PROD-001
```

**Response**: `200 OK`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": "Product deleted successfully"
}
```

---

## Cart Service APIs

**Important**: All cart APIs require authentication. The `memberId` is automatically extracted from the JWT token by the gateway and sent as `X-Member-Id` header to the cart service.

### 9. Add Product to Cart

**Endpoint**: `POST /api/carts/addItems`

**Description**: Adds a product to the user's shopping cart. Requires authentication.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer <jwt-token>
```

**Note**: The gateway automatically extracts `memberId` from the token and adds it as `X-Member-Id` header.

**Request Body**:
```json
{
  "productId": "PROD-001",
  "quantity": 2
}
```

**Request Fields**:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| productId | String | Yes | - | Product identifier |
| quantity | Integer | Yes | Must be > 0 | Quantity to add |

**Response**: `200 OK`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "memberId": 1,
    "items": [
      {
        "productId": "PROD-001",
        "productName": "iPhone 15",
        "productImageUrl": "https://example.com/iphone.jpg",
        "productPrice": 999.99,
        "quantity": 2,
        "itemPrice": 1999.98
      }
    ],
    "totalPrice": 1999.98
  }
}
```

**Error Responses**:

- **401 Unauthorized** - Missing/Invalid Token
```json
{
  "success": false,
  "errorMessage": "Invalid or missing token",
  "errorCode": "UNAUTHORIZED",
  "value": null
}
```

- **400 Bad Request** - Product Not Found
```json
{
  "success": false,
  "errorMessage": "Product not found with id: PROD-99999",
  "errorCode": "BAD_REQUEST",
  "value": null
}
```

---

### 10. View Cart

**Endpoint**: `GET /api/carts/getCart`

**Description**: Retrieves the current user's shopping cart with all items.

**Request Headers**:
```
Authorization: Bearer <jwt-token>
```

**Note**: The gateway automatically extracts `memberId` from the token and adds it as `X-Member-Id` header.

**Response**: `200 OK` (Cart with Items)
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "memberId": 1,
    "items": [
      {
        "productId": "PROD-001",
        "productName": "iPhone 15",
        "productImageUrl": "https://example.com/iphone.jpg",
        "productPrice": 999.99,
        "quantity": 2,
        "itemPrice": 1999.98
      },
      {
        "productId": "PROD-002",
        "productName": "MacBook Pro 16",
        "productImageUrl": "https://example.com/macbook.jpg",
        "productPrice": 2499.99,
        "quantity": 1,
        "itemPrice": 2499.99
      }
    ],
    "totalPrice": 4499.97
  }
}
```

**Response**: `200 OK` (Empty Cart)
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "memberId": 1,
    "items": [],
    "totalPrice": 0.00
  }
}
```

**Error Responses**:

- **401 Unauthorized** - Missing/Invalid Token
```json
{
  "success": false,
  "errorMessage": "Invalid or missing token",
  "errorCode": "UNAUTHORIZED",
  "value": null
}
```

---

### 11. Remove Product from Cart

**Endpoint**: `DELETE /api/carts/removeItems`

**Description**: Removes a specific product from the user's shopping cart.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer <jwt-token>
```

**Note**: The gateway automatically extracts `memberId` from the token and adds it as `X-Member-Id` header.

**Request Body**:
```json
{
  "productId": "PROD-001"
}
```

**Request Fields**:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| productId | String | Yes | Product identifier to remove |

**Response**: `200 OK`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": {
    "memberId": 1,
    "items": [
      {
        "productId": "PROD-002",
        "productName": "MacBook Pro 16",
        "productImageUrl": "https://example.com/macbook.jpg",
        "productPrice": 2499.99,
        "quantity": 1,
        "itemPrice": 2499.99
      }
    ],
    "totalPrice": 2499.99
  }
}
```

**Error Responses**:

- **401 Unauthorized** - Missing/Invalid Token
```json
{
  "success": false,
  "errorMessage": "Invalid or missing token",
  "errorCode": "UNAUTHORIZED",
  "value": null
}
```

- **400 Bad Request** - Product Not in Cart
```json
{
  "success": false,
  "errorMessage": "Product not found in cart: PROD-99999",
  "errorCode": "BAD_REQUEST",
  "value": null
}
```

---

### 12. Clear Cart

**Endpoint**: `DELETE /api/carts/clearCart`

**Description**: Clears all items from the user's shopping cart.

**Request Headers**:
```
Authorization: Bearer <jwt-token>
```

**Note**: The gateway automatically extracts `memberId` from the token and adds it as `X-Member-Id` header.

**Response**: `200 OK`
```json
{
  "success": true,
  "errorMessage": null,
  "errorCode": null,
  "value": "Cart cleared successfully"
}
```

**Error Responses**:

- **401 Unauthorized** - Missing/Invalid Token
```json
{
  "success": false,
  "errorMessage": "Invalid or missing token",
  "errorCode": "UNAUTHORIZED",
  "value": null
}
```

---

## Error Responses

### Standard Error Format

All error responses follow this structure:

```json
{
  "success": false,
  "errorMessage": "Human-readable error message",
  "errorCode": "ERROR_CODE",
  "value": null
}
```

### Common Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `BAD_REQUEST` | 400 | Invalid request parameters |
| `UNAUTHORIZED` | 401 | Authentication required or token invalid |
| `NOT_FOUND` | 404 | Resource not found |
| `INTERNAL_SERVER_ERROR` | 500 | Server error occurred |

### Error Response Examples

**400 Bad Request - Validation Error**:
```json
{
  "success": false,
  "errorMessage": "Validation failed: Email must be valid",
  "errorCode": "VALIDATION_ERROR",
  "value": null
}
```

**401 Unauthorized - Invalid Token**:
```json
{
  "success": false,
  "errorMessage": "Invalid or missing token",
  "errorCode": "UNAUTHORIZED",
  "value": null
}
```

**404 Not Found**:
```json
{
  "success": false,
  "errorMessage": "Product not found with id: PROD-99999",
  "errorCode": "BAD_REQUEST",
  "value": null
}
```

**500 Internal Server Error**:
```json
{
  "success": false,
  "errorMessage": "An unexpected error occurred: [error details]",
  "errorCode": "INTERNAL_SERVER_ERROR",
  "value": null
}
```

---

## Database Schema

### Members Table (PostgreSQL)

**Table Name**: `members`

**Reason for PostgreSQL**:
- No duplicate emails (Data integrity)
- All users see the same data (Data consistency)
- Safe updates (Transaction support)

| Field | Type | Description |
|-------|------|-------------|
| member_id | BIGINT | Unique user ID (Primary Key, Auto-generated) |
| email | VARCHAR(255) | User email (must be unique) |
| password | TEXT | Encrypted password (BCrypt) |
| name | VARCHAR(255) | User's full name |
| phone | VARCHAR(20) | Contact number |
| created_at | TIMESTAMP | When account was created |
| updated_at | TIMESTAMP | Last update time |

### Products Collection (MongoDB)

**Collection Name**: `products`

**Reason for MongoDB**:
- Flexible schema since different products may have different attributes
- Built-in text search
- Handles large number of products/documents efficiently

**Example Document**:
```json
{
  "productId": "PROD-001",
  "name": "iPhone 15",
  "description": "Latest iPhone with advanced features",
  "category": "Electronics",
  "price": 999.99,
  "brand": "Apple",
  "tags": ["smartphone", "apple", "electronics"],
  "attributes": {
    "color": "Black",
    "storage": "128GB",
    "ram": "6GB"
  },
  "imageUrl": "https://example.com/iphone.jpg",
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Carts Collection (MongoDB)

**Collection Name**: `carts`

**Reason for MongoDB**:
- Easy to add/remove cart items
- Entire cart stored as one document
- Flexible structure

**Example Document**:
```json
{
  "id": "cart-001",
  "memberId": 1,
  "items": [
    {
      "productId": "PROD-001",
      "quantity": 2,
      "addedAt": "2024-01-01T00:00:00Z"
    },
    {
      "productId": "PROD-002",
      "quantity": 1,
      "addedAt": "2024-01-01T00:00:00Z"
    }
  ],
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

**Note**: Product details (name, image, price) are fetched from Product Service when viewing cart, not stored in cart document.

---

## Redis Cache

### Cache Strategy

| Cache Type | TTL | Purpose |
|------------|-----|---------|
| Product Search Results | 5-10 minutes | Fast product search responses |
| Product Details | 30 minutes | Quick product detail retrieval |
| User Information | 30 minutes | Fast user data access |

### Why Redis?

- **Very fast** (in-memory caching)
- **Reduces database load**
- **Improves response time**

---

## Authentication Flow

### Registration Flow

```
User → API Gateway → Member Service → PostgreSQL
      (routes)      (hashes password) (saves user)
      ← Success (200 OK)
```

### Login Flow

```
User → API Gateway → Member Service → PostgreSQL
      (routes)      (verifies password) (checks user)
      ← User data
      (creates JWT token in LoginResponseFilter)
      ← Token in response + User data
```

### Add to Cart Flow

```
User → API Gateway → Cart Service → MongoDB
      (validates JWT) (adds item)   (saves cart)
      (extracts memberId)
      (adds X-Member-Id header)
      ← Success (200 OK)
```

---

## Notes

1. **All requests should go through the API Gateway** (port 8092) unless testing services directly.

2. **JWT tokens are generated by the gateway** after successful login. The member service does not generate tokens.

3. **Cart APIs automatically extract `memberId`** from the JWT token. No need to pass it in the URL or request body.

4. **Product APIs are public** and do not require authentication.

5. **Cart APIs require authentication** - Include JWT token in `Authorization` header.

6. **Response format is standardized** - All responses use `ApiResponse<T>` wrapper.

7. **Pagination** - Page numbers start from 0, default page size is 10.

8. **Error handling** - All errors return consistent format with `success: false` and appropriate error codes.

---

## Support

For issues or questions, please refer to the service logs or contact the development team.

