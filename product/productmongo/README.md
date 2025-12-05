# Product Service - MongoDB Integration

A Spring Boot microservice for managing products with MongoDB database and Redis caching.

## Features

- **Product Search with Pagination**: Search products with wildcard support across name, description, and category
- **Product Details**: Get complete product information by ID
- **Product Creation**: Create new products
- **Redis Caching**: Improved performance with Redis cache
- **MongoDB**: NoSQL database for flexible product storage
- **Swagger UI**: Interactive API documentation

## Prerequisites

- Java 21
- MongoDB (running on localhost:27017)
- Redis (running on localhost:6379)
- Maven

## Project Structure

```
src/main/java/com/training/productService/productmongo/
├── controller/
│   ├── ProductController.java              # Search products endpoint
│   ├── ProductDetailsController.java       # Get product by ID endpoint
│   └── CreateProductController.java        # Create product endpoint
├── service/
│   ├── ProductService.java                 # Service interface
│   └── ProductServiceImpl.java             # Service implementation
├── repository/
│   ├── ProductRepository.java              # MongoDB repository
│   ├── ProductCustomRepository.java        # Custom repository interface
│   └── ProductCustomRepositoryImpl.java    # Custom repository implementation
├── entity/
│   └── Product.java                        # Product entity
├── dto/
│   ├── ProductDTO.java                     # Product DTO
│   ├── CreateProductRequest.java           # Create product request DTO
│   └── ProductPageResponse.java            # Paginated response DTO
├── model/
│   ├── ApiResponse.java                    # Common success response
│   └── ErrorResponse.java                  # Common error response
├── exception/
│   ├── ProductNotFoundException.java       # Custom exception
│   └── GlobalExceptionHandler.java         # Global exception handler
└── config/
    └── SwaggerConfig.java                  # Swagger configuration
```

## API Endpoints

### 1. Search Products with Pagination

**Endpoint**: `POST /product?page=0&size=10&searchTerm=MacBook`

**Description**: Search products with pagination and optional search term

**Query Parameters**:
- `page` (optional, default: 0) - Page number (0-indexed)
- `size` (optional, default: 10) - Page size
- `searchTerm` (optional) - Search term for filtering products

**Success Response** (200 OK):
```json
{
  "content": [
    {
      "id": "507f1f77bcf86cd799439011",
      "name": "MacBook Pro 14",
      "description": "M3 Pro chip, 16GB RAM, 512GB SSD",
      "price": 1999.00,
      "category": "Electronics",
      "tags": ["laptop", "apple", "m3"],
      "images": ["https://example.com/mbp14.jpg"]
    },
    {
      "id": "507f1f77bcf86cd799439012",
      "name": "MacBook Air 15",
      "description": "M2 chip, 8GB RAM, 256GB SSD",
      "price": 1299.00,
      "category": "Electronics",
      "tags": ["laptop", "apple", "m2"],
      "images": ["https://example.com/mba15.jpg"]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 2
  },
  "totalElements": 50,
  "totalPages": 25,
  "last": false
}
```

**Error Response** (400 Bad Request):
```json
{
  "errorCode": "400",
  "errorMessage": "Product not found with the search term"
}
```

**Error Response** (500 Internal Server Error):
```json
{
  "errorCode": "500",
  "errorMessage": "Internal server error"
}
```

### 2. Get Product by ID

**Endpoint**: `GET /product/{id}`

**Description**: Get complete product details by ID

**Path Parameters**:
- `id` (required) - Product ID

**Success Response** (200 OK):
```json
{
  "id": "507f1f77bcf86cd799439011",
  "name": "MacBook Pro 14",
  "description": "M3 Pro chip, 16GB RAM, 512GB SSD",
  "price": 1999.00,
  "category": "Electronics",
  "tags": ["laptop", "apple", "m3"],
  "images": ["https://example.com/mbp14-1.jpg", "https://example.com/mbp14-2.jpg"]
}
```

**Error Response** (400 Bad Request):
```json
{
  "errorCode": "400",
  "errorMessage": "Product not found in the system"
}
```

**Error Response** (500 Internal Server Error):
```json
{
  "errorCode": "500",
  "errorMessage": "Internal server error"
}
```

### 3. Create Product

**Endpoint**: `POST /createProduct`

**Description**: Create a new product

**Request Body**:
```json
{
  "name": "MacBook Pro 14",
  "description": "M3 Pro chip, 16GB RAM, 512GB SSD",
  "price": 1999.00,
  "category": "Electronics",
  "tags": ["laptop", "apple", "m3"],
  "images": ["https://example.com/mbp14.jpg"]
}
```

**Success Response** (201 Created):
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "name": "MacBook Pro 14",
    "description": "M3 Pro chip, 16GB RAM, 512GB SSD",
    "price": 1999.00,
    "category": "Electronics",
    "tags": ["laptop", "apple", "m3"],
    "images": ["https://example.com/mbp14.jpg"]
  }
}
```

**Error Response** (400 Bad Request):
```json
{
  "errorCode": "400",
  "errorMessage": "Product name is required"
}
```

**Error Response** (500 Internal Server Error):
```json
{
  "errorCode": "500",
  "errorMessage": "Internal server error"
}
```

## Running the Application

### 1. Start MongoDB

```bash
# Using Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Or start MongoDB service if installed locally
mongod
```

### 2. Start Redis

```bash
# Using Docker
docker run -d -p 6379:6379 --name redis redis:latest

# Or start Redis service if installed locally
redis-server
```

### 3. Build and Run the Application

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8082`

### 4. Access Swagger UI

Open your browser and navigate to:
```
http://localhost:8082/swagger-ui/
```

## Testing the APIs

### Using cURL

**Search Products**:
```bash
curl -X POST "http://localhost:8082/product?page=0&size=10&searchTerm=MacBook"
```

**Get Product by ID**:
```bash
curl -X GET "http://localhost:8082/product/507f1f77bcf86cd799439011"
```

**Create Product**:
```bash
curl -X POST "http://localhost:8082/createProduct" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro 14",
    "description": "M3 Pro chip, 16GB RAM, 512GB SSD",
    "price": 1999.00,
    "category": "Electronics",
    "tags": ["laptop", "apple", "m3"],
    "images": ["https://example.com/mbp14.jpg"]
  }'
```

## Configuration

Edit `src/main/resources/application.properties` to configure:

- **Server Port**: `server.port=8082`
- **MongoDB**: `spring.data.mongodb.host`, `spring.data.mongodb.port`, `spring.data.mongodb.database`
- **Redis**: `spring.redis.host`, `spring.redis.port`
- **Cache TTL**: `spring.cache.redis.time-to-live`

## Key Features

### 1. Wildcard Search
The search functionality supports case-insensitive wildcard search across:
- Product name
- Product description
- Product category

### 2. Caching
- Product search results are cached in Redis
- Individual product details are cached
- Cache is automatically invalidated when new products are created

### 3. Exception Handling
- Global exception handler for consistent error responses
- Custom exceptions for domain-specific errors
- Proper HTTP status codes for different error scenarios

### 4. Validation
- Request validation for required fields
- Business logic validation (e.g., price > 0)
- Pagination parameter validation

## Architecture

The application follows a layered architecture:

1. **Controller Layer**: Handles HTTP requests and responses (3 separate controllers)
2. **Service Layer**: Contains business logic (interface + implementation)
3. **Repository Layer**: Data access layer with custom MongoDB queries
4. **Model Layer**: Common response and error models
5. **Exception Layer**: Centralized exception handling

## Technologies Used

- **Spring Boot 2.2.1**: Application framework
- **Spring Data MongoDB**: MongoDB integration
- **Spring Cache**: Caching abstraction
- **Redis**: Cache storage
- **Lombok**: Reduce boilerplate code
- **Springfox Swagger**: API documentation
- **Maven**: Build tool
