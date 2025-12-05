# Marketplace API Documentation

## Base URL
All API requests go through the API Gateway at `http://localhost:8080`

## Authentication
Most endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-token>
```

---

## Member Service APIs

### Register
Create a new member account.

**POST** `/api/members/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+6281234567890",
  "address": "123 Main St, Jakarta"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "phoneNumber": "+6281234567890",
    "address": "123 Main St, Jakarta",
    "active": true,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### Login
Authenticate and get JWT tokens.

**POST** `/api/members/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "member": {
      "id": 1,
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe"
    }
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### Logout
Invalidate the current JWT token.

**POST** `/api/members/logout`

**Headers:**
```
Authorization: Bearer <your-token>
```

**Response:**
```json
{
  "success": true,
  "message": "Logout successful",
  "timestamp": "2024-01-01T10:00:00"
}
```

### Get Current Member
Get the authenticated member's profile.

**GET** `/api/members/me`

**Headers:**
```
Authorization: Bearer <your-token>
```

---

## Product Service APIs

### List Products
Get paginated list of all active products.

**GET** `/api/products`

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number (0-based) |
| size | int | 10 | Items per page |
| sortBy | string | createdAt | Field to sort by |
| sortDirection | string | desc | Sort direction (asc/desc) |

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "prod-001",
      "name": "Test Product",
      "description": "Product description",
      "category": "Electronics",
      "brand": "TestBrand",
      "price": 99.99,
      "originalPrice": 129.99,
      "discountPercentage": 20,
      "images": ["https://example.com/image.jpg"],
      "tags": ["electronics", "new"],
      "rating": 4.5,
      "reviewCount": 100,
      "active": true
    }
  ],
  "pageInfo": {
    "page": 0,
    "size": 10,
    "totalElements": 50000,
    "totalPages": 5000,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### Search Products
Search products with wildcard support.

**GET** `/api/products/search`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| keyword | string | Search keyword (supports wildcard) |
| category | string | Filter by category |
| brand | string | Filter by brand |
| page | int | Page number |
| size | int | Items per page |
| sortBy | string | Sort field |
| sortDirection | string | Sort direction |

### Get Product by ID
Get product details by ID.

**GET** `/api/products/{id}`

### Get Products by IDs (Batch)
Get multiple products by their IDs.

**POST** `/api/products/batch`

**Request Body:**
```json
["prod-001", "prod-002", "prod-003"]
```

---

## Cart Service APIs

### Get Cart
Get the current user's shopping cart.

**GET** `/api/cart`

**Headers:**
```
Authorization: Bearer <your-token>
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "memberId": 1,
    "items": [
      {
        "id": 1,
        "productId": "prod-001",
        "productName": "Test Product",
        "price": 99.99,
        "quantity": 2,
        "subtotal": 199.98,
        "productImage": "https://example.com/image.jpg"
      }
    ],
    "totalAmount": 199.98,
    "totalItems": 2
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### Add to Cart
Add a product to the shopping cart.

**POST** `/api/cart/items`

**Headers:**
```
Authorization: Bearer <your-token>
```

**Request Body:**
```json
{
  "productId": "prod-001",
  "productName": "Test Product",
  "price": 99.99,
  "quantity": 2,
  "productImage": "https://example.com/image.jpg"
}
```

### Update Cart Item
Update the quantity of a cart item.

**PUT** `/api/cart/items/{productId}`

**Headers:**
```
Authorization: Bearer <your-token>
```

**Request Body:**
```json
{
  "quantity": 5
}
```

### Remove from Cart
Remove a product from the cart.

**DELETE** `/api/cart/items/{productId}`

**Headers:**
```
Authorization: Bearer <your-token>
```

### Clear Cart
Remove all items from the cart.

**DELETE** `/api/cart`

**Headers:**
```
Authorization: Bearer <your-token>
```

---

## Error Responses

All error responses follow this format:

```json
{
  "success": false,
  "message": "Error message here",
  "timestamp": "2024-01-01T10:00:00"
}
```

### Common HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid token) |
| 404 | Not Found |
| 500 | Internal Server Error |

