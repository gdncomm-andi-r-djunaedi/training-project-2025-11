# Cart Service - Overview Documentation

## Introduction

The Cart Service is a Spring Boot-based microservice designed to manage shopping cart functionality for an online marketplace application. It provides a RESTful API for adding items to cart, viewing cart contents, and removing items from the cart. The service integrates with the Product Service using Feign Client to fetch product details and pricing information.

This service follows microservice architecture best practices, including separation of concerns, DTO pattern, and repository abstraction. It uses MongoDB for data persistence and implements comprehensive error handling for robust operation.

---

## Topics to Learn

The following topics and concepts are demonstrated in this Cart Service application. Use this as a learning guide to understand the technologies and patterns used:

**Spring Framework & Spring Boot**: Spring Boot Application, Spring Boot Auto Configuration, Spring Boot Starter Dependencies, Spring Context, Dependency Injection, Bean Management, Component Scanning, Configuration Classes, Spring Profiles

**RESTful Web Services**: REST API Design, HTTP Methods (GET, POST, DELETE), Request Mapping, Path Variables, Request Body, Response Entity, HTTP Status Codes, RESTful Endpoints

**Spring Data MongoDB**: MongoDB Integration, MongoRepository, Document Mapping, Embedded Documents, Repository Pattern, CRUD Operations, Query Methods, MongoDB Collections

**Microservices Architecture**: Service-to-Service Communication, Feign Client, Inter-Service Calls, Microservice Patterns, Service Independence, Distributed Systems

**Spring Cloud OpenFeign**: Feign Client Interface, Declarative REST Clients, Service Communication, HTTP Client Abstraction, Feign Exception Handling, Service Discovery

**Data Transfer Objects (DTO)**: DTO Pattern, Entity-DTO Separation, Request DTOs, Response DTOs, Data Transformation, API Contract Design

**Exception Handling**: Global Exception Handler, @RestControllerAdvice, Exception Mapping, Error Response Formatting, HTTP Status Code Mapping, Custom Exceptions, Feign Exception Handling, Validation Exception Handling

**Validation**: Jakarta Bean Validation, @Positive, @NotNull, @NotBlank, Input Validation, Request Validation, Validation Error Handling

**Lombok**: @Data, @Builder, @RequiredArgsConstructor, @AllArgsConstructor, @NoArgsConstructor, @Slf4j, Code Generation, Boilerplate Reduction

**Logging**: SLF4J, Log4j, Logging Levels (INFO, ERROR, DEBUG), Structured Logging, Logging Best Practices

**MongoDB**: NoSQL Database, Document Store, Embedded Documents, Collections, Document ID, UUID as Primary Key, MongoDB Operations

**Java 21 Features**: Modern Java Syntax, Stream API, Optional, Lambda Expressions, Method References, var Keyword, Records (if used)

**Builder Pattern**: Lombok Builder, Object Construction, Fluent API, Immutable Objects

**Optional & Stream API**: Optional.orElse(), Optional.orElseThrow(), Stream.filter(), Stream.mapToDouble(), Stream.sum(), findFirst(), removeIf()

**Generic Types**: GenericResponse<T>, Type Parameters, Generic Methods, Type Safety

**UUID**: UUID Generation, UUID as Primary Key, UUID Parsing, UUID Validation

**OpenAPI/Swagger**: API Documentation, Swagger UI, OpenAPI Specification, API Contract Documentation

**Maven**: Maven Project Structure, pom.xml, Dependencies Management, Maven Build Lifecycle, Maven Wrapper

**Layered Architecture**: Controller Layer, Service Layer, Repository Layer, Entity Layer, DTO Layer, Separation of Concerns, Clean Architecture

**Error Response Design**: Consistent Error Format, Generic Error Response, Error Status Codes, Error Messages

**Service Layer Pattern**: Service Interface, Service Implementation, Business Logic Separation, Transaction Management

**Repository Pattern**: Data Access Abstraction, Repository Interface, CRUD Abstraction

---

## Features

### Core Functionality
- **Add to Cart**: Add products to user's shopping cart with quantity specification
- **View Cart**: Retrieve complete cart details including all items and total price
- **Remove from Cart**: Remove specific products from the cart
- **Automatic Price Calculation**: Automatically calculates item totals and cart total based on product prices
- **Quantity Management**: Supports updating quantities when adding existing products to cart
- **Product Integration**: Fetches real-time product information from Product Service via Feign Client

### Technical Features
- Input validation using Jakarta Bean Validation
- Structured response wrapper (`GenericResponse`) for consistent API responses
- Comprehensive error handling with global exception handler
- MongoDB document-based storage for cart persistence
- DTO pattern for data transfer and entity separation
- OpenAPI/Swagger integration for interactive API documentation
- Feign Client integration for inter-service communication

---

## Technology Stack

### Core Framework
- **Spring Boot**: `3.4.12`
- **Java**: `21`
- **Build Tool**: Maven

### Dependencies
- **Spring Boot Starter Web**: RESTful web services
- **Spring Boot Starter Data MongoDB**: MongoDB integration
- **Spring Cloud OpenFeign**: Inter-service communication with Product Service
- **Lombok**: Reduces boilerplate code
- **SpringDoc OpenAPI**: API documentation (Swagger UI)
- **Jakarta Bean Validation**: Input validation

### Database
- **MongoDB**: NoSQL database for cart storage

### Development Tools
- **Lombok**: Code generation for getters, setters, constructors, builders
- **SLF4J**: Logging framework

---

## Architecture

The application follows a **layered architecture** pattern with clear separation of concerns:

```
┌─────────────────────────────────────┐
│         Controller Layer             │
│      (CartController)                │
│   - Handles HTTP requests/responses  │
│   - Input validation                 │
│   - Response formatting              │
└──────────────┬───────────────────────┘
               │
┌──────────────▼───────────────────────┐
│          Service Layer                │
│      (CartService/CartServiceImpl)   │
│   - Business logic                   │
│   - Cart operations                  │
│   - Product service integration      │
│   - DTO conversion                   │
└──────────────┬───────────────────────┘
               │
       ┌───────┴────────┐
       │                │
┌──────▼──────┐  ┌─────▼──────────────┐
│  Repository │  │  Feign Client       │
│   Layer     │  │  (ProductFeign)     │
│(CartRepo)   │  │  - Product Service  │
│             │  │    Communication    │
└──────┬──────┘  └─────────────────────┘
       │
┌──────▼───────────────────────────────┐
│           MongoDB                     │
│      (Cart Data Persistence)          │
└──────────────────────────────────────┘
```

### Component Details

1. **Controller Layer** (`CartController`)
   - RESTful endpoints for cart operations
   - Request/response mapping
   - HTTP status code management
   - Base path: `/api/cart`

2. **Service Layer** (`CartService` / `CartServiceImpl`)
   - Business logic implementation
   - Cart creation and management
   - Product price fetching via Feign Client
   - Quantity aggregation for duplicate products
   - Total price calculation
   - Entity to DTO conversion

3. **Repository Layer** (`CartRepository`)
   - Extends `MongoRepository<Cart, UUID>`
   - CRUD operations for cart entities
   - Uses UUID as primary key (userId)

4. **Entity Layer** (`Cart`, `CartItem`)
   - MongoDB document mapping
   - Validation constraints
   - Cart structure with items list

5. **DTO Layer** (`CartResponseDTO`, `CartItemDTO`, `AddToCartRequest`)
   - Data transfer objects
   - Input validation
   - Serialization support

6. **Feign Client** (`ProductFeignClient`)
   - Inter-service communication
   - Fetches product details from Product Service
   - Handles product service responses

7. **Exception Handling** (`GlobalExceptionHandler`)
   - Centralized error handling
   - Consistent error response format
   - Handles Feign exceptions, validation errors, and custom exceptions

8. **Configuration** (`OpenApiConfig`)
   - OpenAPI/Swagger configuration

---

## API Endpoints

Base URL: `http://localhost:8081/api/cart`

### 1. Add Item to Cart

**Endpoint**: `POST /{userId}/add`

**Description**: Adds a product to the user's cart. If the product already exists in the cart, the quantity is updated. The service fetches product details from the Product Service to get the current price.

**Path Parameters**:
- `userId` (UUID): Unique identifier for the user

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "productId": "67890abcdef1234567890123",
  "quantity": 2
}
```

**Response** (200 OK):
```json
{
  "status": "SUCCESS",
  "message": "Item added to cart",
  "data": {
    "totalCartPrice": 2599.98,
    "items": [
      {
        "productId": "67890abcdef1234567890123",
        "quantity": 2,
        "price": 2599.98
      }
    ]
  }
}
```

**Validation Rules**:
- `productId`: Required, must be a valid product ID from Product Service
- `quantity`: Required, must be a positive integer

**Error Scenarios**:
- Product not found in Product Service: Returns 404 or 503
- Invalid productId format: Returns 400
- Product service unavailable: Returns 503

---

### 2. View Cart

**Endpoint**: `GET /{userId}`

**Description**: Retrieves the complete cart details for a specific user, including all items and the total cart price.

**Path Parameters**:
- `userId` (UUID): Unique identifier for the user

**Example Request**:
```
GET /api/cart/550e8400-e29b-41d4-a716-446655440000
```

**Response** (200 OK):
```json
{
  "status": "SUCCESS",
  "message": "Cart fetched",
  "data": {
    "totalCartPrice": 3599.97,
    "items": [
      {
        "productId": "67890abcdef1234567890123",
        "quantity": 2,
        "price": 2599.98
      },
      {
        "productId": "67890abcdef1234567890124",
        "quantity": 1,
        "price": 999.99
      }
    ]
  }
}
```

**Error Response** (404 Not Found):
```json
{
  "status": "ERROR",
  "message": "Cart not found",
  "data": null
}
```

---

### 3. Remove Item from Cart

**Endpoint**: `DELETE /{userId}/remove/{productId}`

**Description**: Removes a specific product from the user's cart. The cart total is automatically recalculated after removal.

**Path Parameters**:
- `userId` (UUID): Unique identifier for the user
- `productId` (String): Product ID to remove from cart

**Example Request**:
```
DELETE /api/cart/550e8400-e29b-41d4-a716-446655440000/remove/67890abcdef1234567890123
```

**Response** (200 OK):
```json
{
  "status": "SUCCESS",
  "message": "Item removed",
  "data": {
    "totalCartPrice": 999.99,
    "items": [
      {
        "productId": "67890abcdef1234567890124",
        "quantity": 1,
        "price": 999.99
      }
    ]
  }
}
```

**Error Response** (404 Not Found):
```json
{
  "status": "ERROR",
  "message": "Cart not found",
  "data": null
}
```

---

## Data Models

### Cart Entity

The `Cart` entity represents the MongoDB document structure:

```java
@Document(collection = "cart")
public class Cart {
    @Id
    private UUID cartId;                    // User ID as cart identifier
    
    @Positive(message = "Price must be greater than zero")
    private Double totalCartPrice;          // Total price of all items
    
    private List<CartItem> items;          // List of cart items
}
```

**Database Collection**: `cart`

**Key Characteristics**:
- Uses UUID as the document ID (userId)
- Contains a list of `CartItem` objects
- Maintains total cart price for quick access

---

### CartItem Entity

The `CartItem` entity represents individual items in the cart:

```java
public class CartItem {
    private String productId;              // Product identifier
    
    @Positive(message = "Quantity should not be less than 0")
    private int quantity;                  // Item quantity
    
    @Positive(message = "Price must be greater than zero")
    private double itemTotalPrice;         // Total price for this item (quantity * unit price)
}
```

**Key Characteristics**:
- Embedded document within Cart
- Stores product ID reference
- Calculates item total based on quantity and unit price

---

### CartResponseDTO

The `CartResponseDTO` is used for API responses:

```java
public class CartResponseDTO {
    private double totalCartPrice;         // Total cart price
    private List<CartItemDTO> items;       // List of cart items
}
```

---

### CartItemDTO

The `CartItemDTO` represents cart items in API responses:

```java
public class CartItemDTO {
    private String productId;              // Product identifier
    private int quantity;                  // Item quantity
    private double price;                  // Total price for this item
}
```

---

### AddToCartRequest

The `AddToCartRequest` is used for adding items to cart:

```java
public class AddToCartRequest {
    private String productId;              // Product identifier
    private int quantity;                  // Quantity to add
}
```

---

### GenericResponse

Standardized response wrapper for all API endpoints:

```java
public class GenericResponse<T> {
    private String status;                // "SUCCESS" or "ERROR"
    private String message;               // Descriptive message
    private T data;                       // Response payload (generic type)
}
```

**Usage**:
- All endpoints return `GenericResponse<CartResponseDTO>`
- Provides consistent response structure
- Includes status, message, and data fields

---

### ProductDTO

The `ProductDTO` represents product data fetched from Product Service:

```java
public class ProductDTO {
    private String productId;
    private String productName;
    private String description;
    
    @JsonProperty("price")
    private Double price;                  // Product unit price
    
    private String category;
    private List<String> images;
    
    // Helper method
    public double getProductUnitPrice() {
        return price != null ? price : 0.0;
    }
}
```

---

### ProductResponseWrapper

Wrapper for Product Service response:

```java
public class ProductResponseWrapper {
    private boolean success;
    private String message;
    private ProductDTO data;
}
```

---

## Error Handling

The Cart Service implements comprehensive error handling through a global exception handler (`GlobalExceptionHandler`).

### Exception Types

#### 1. CartNotFoundException

**HTTP Status**: `404 NOT FOUND`

**Trigger**: When attempting to view or modify a cart that doesn't exist

**Response Example**:
```json
{
  "status": "ERROR",
  "message": "Cart not found",
  "data": null
}
```

---

#### 2. Feign Client Exceptions

**HTTP Status**: `404 NOT FOUND`, `503 SERVICE UNAVAILABLE`, or `502 BAD GATEWAY`

**Trigger**: When Product Service is unreachable or returns errors

**Response Examples**:

**Product Not Found (404)**:
```json
{
  "status": "ERROR",
  "message": "Product not found in product service",
  "data": null
}
```

**Service Unavailable (503)**:
```json
{
  "status": "ERROR",
  "message": "Product service is currently unavailable. Please try again later.",
  "data": null
}
```

**Bad Gateway (502)**:
```json
{
  "status": "ERROR",
  "message": "Error communicating with product service",
  "data": null
}
```

---

#### 3. Validation Errors

**HTTP Status**: `400 BAD REQUEST`

**Trigger**: When request validation fails (e.g., invalid UUID format, negative quantity)

**Response Example**:
```json
{
  "status": "ERROR",
  "message": "Validation failed",
  "data": {
    "quantity": "Quantity should not be less than 0",
    "totalCartPrice": "Price must be greater than zero"
  }
}
```

---

#### 4. Type Mismatch Errors

**HTTP Status**: `400 BAD REQUEST`

**Trigger**: When path parameters have incorrect types (e.g., invalid UUID format)

**Response Example**:
```json
{
  "status": "ERROR",
  "message": "Invalid value 'invalid-uuid' for parameter 'userId'. Expected type: UUID",
  "data": null
}
```

---

#### 5. Illegal Argument Exceptions

**HTTP Status**: `400 BAD REQUEST`

**Trigger**: When invalid arguments are passed to methods

**Response Example**:
```json
{
  "status": "ERROR",
  "message": "Product not found or price not available",
  "data": null
}
```

---

#### 6. General Exceptions

**HTTP Status**: `500 INTERNAL SERVER ERROR`

**Trigger**: For any unexpected errors

**Response Example**:
```json
{
  "status": "ERROR",
  "message": "An unexpected error occurred. Please try again later.",
  "data": null
}
```

---

### Error Handling Strategy

1. **Global Exception Handler**:
   - Centralized error handling using `@RestControllerAdvice`
   - Consistent error response format
   - Appropriate HTTP status codes

2. **Service Layer**:
   - Validates product existence via Feign Client
   - Throws `CartNotFoundException` for missing carts
   - Handles product service communication errors

3. **Logging**:
   - Comprehensive logging using SLF4J
   - Log levels: INFO, ERROR
   - Includes context information (userId, productId, etc.)

4. **Error Response Format**:
   - All errors return `GenericResponse<Object>`
   - Consistent structure across all error types
   - Descriptive error messages

---

## Configuration

### Application Properties

The application configuration is defined in `application.properties`:

```properties
# Application Name
spring.application.name=cart

# Server Configuration
server.port=8081

# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=test
spring.data.mongodb.repositories.enabled=true

# Logging Configuration
logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG
```

### Configuration Details

#### MongoDB
- **Host**: `localhost`
- **Port**: `27017`
- **Database**: `test`
- **Collection**: `cart` (defined in entity)

#### Server
- **Port**: `8081`
- **Base Path**: `/api/cart`

#### Feign Client Configuration
- **Product Service URL**: `http://localhost:8080`
- **Product Service Endpoint**: `/api/product/{id}`
- Configured in `ProductFeignClient` interface

### OpenAPI/Swagger Configuration

Swagger UI is available at: `http://localhost:8081/swagger-ui.html`

API documentation endpoint: `http://localhost:8081/v3/api-docs`

The OpenAPI configuration is defined in `OpenApiConfig`:
- Title: "Cart"
- Description: "Cart Service"
- Version: "1.0"

---

## Getting Started

### Prerequisites

1. **Java 21** or higher
2. **Maven 3.6+**
3. **MongoDB** (running on localhost:27017)
4. **Product Service** (running on localhost:8080) - Required for cart operations

### Installation Steps

#### 1. Navigate to Cart Directory
```bash
cd /path/to/project/cart
```

#### 2. Install Dependencies
```bash
./mvnw clean install
```

Or on Windows:
```bash
mvnw.cmd clean install
```

#### 3. Start MongoDB
Ensure MongoDB is running on `localhost:27017`:
```bash
# macOS/Linux
mongod

# Or using Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

#### 4. Start Product Service
The Cart Service requires the Product Service to be running. Ensure the Product Service is started on `http://localhost:8080` before starting the Cart Service.

#### 5. Configure Application
Update `application.properties` if your MongoDB or Product Service instances are running on different hosts/ports.

**Important**: Update the Feign Client URL in `ProductFeignClient.java` if Product Service is running on a different port:
```java
@FeignClient(name = "product", url = "http://localhost:8080")
```

#### 6. Run the Application
```bash
./mvnw spring-boot:run
```

Or build and run the JAR:
```bash
./mvnw clean package
java -jar target/cart-0.0.1-SNAPSHOT.jar
```

#### 7. Verify Application
- Application should start on `http://localhost:8081`
- Check Swagger UI: `http://localhost:8081/swagger-ui.html`
- Health check: Verify MongoDB connection in logs
- Verify Product Service connectivity

### Testing the API

#### Using cURL

**Add Item to Cart**:
```bash
curl -X POST http://localhost:8081/api/cart/550e8400-e29b-41d4-a716-446655440000/add \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "67890abcdef1234567890123",
    "quantity": 2
  }'
```

**View Cart**:
```bash
curl http://localhost:8081/api/cart/550e8400-e29b-41d4-a716-446655440000
```

**Remove Item from Cart**:
```bash
curl -X DELETE http://localhost:8081/api/cart/550e8400-e29b-41d4-a716-446655440000/remove/67890abcdef1234567890123
```

#### Using Swagger UI

1. Navigate to `http://localhost:8081/swagger-ui.html`
2. Explore available endpoints
3. Test endpoints directly from the UI
4. View request/response schemas

#### Sample Workflow

1. **Create a product** in Product Service (if not exists)
2. **Add product to cart**:
   ```bash
   POST /api/cart/{userId}/add
   {
     "productId": "product-id-from-product-service",
     "quantity": 2
   }
   ```
3. **View cart**:
   ```bash
   GET /api/cart/{userId}
   ```
4. **Remove item**:
   ```bash
   DELETE /api/cart/{userId}/remove/{productId}
   ```

### Development Tips

1. **Logging**: Check application logs for debugging information
2. **MongoDB Compass**: Use MongoDB Compass to view cart documents
3. **Postman/Insomnia**: Import API endpoints for easier testing
4. **Product Service**: Ensure Product Service is running before testing cart operations
5. **UUID Format**: Use valid UUID format for userId (e.g., `550e8400-e29b-41d4-a716-446655440000`)

### Troubleshooting

**Issue**: Application fails to start
- **Solution**: Verify MongoDB is running and accessible

**Issue**: Connection refused to MongoDB
- **Solution**: Check MongoDB host and port in `application.properties`

**Issue**: Product Service connection errors
- **Solution**: Ensure Product Service is running on `http://localhost:8080`

**Issue**: Port 8081 already in use
- **Solution**: Change `server.port` in `application.properties` or stop the conflicting service

**Issue**: Cart operations fail with Feign errors
- **Solution**: Verify Product Service is accessible and the endpoint `/api/product/{id}` is working

**Issue**: Product not found errors
- **Solution**: Ensure the product exists in Product Service before adding to cart

---

## Additional Information

### Project Structure
```
cart/
├── src/
│   ├── main/
│   │   ├── java/com/blibi/cart/
│   │   │   ├── CartApplication.java
│   │   │   ├── controller/
│   │   │   │   └── CartController.java
│   │   │   ├── service/
│   │   │   │   └── CartService.java
│   │   │   ├── serviceImpl/
│   │   │   │   └── CartServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── CartRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Cart.java
│   │   │   │   └── CartItem.java
│   │   │   ├── dto/
│   │   │   │   ├── CartResponseDTO.java
│   │   │   │   ├── CartItemDTO.java
│   │   │   │   ├── AddToCartRequest.java
│   │   │   │   ├── GenericResponse.java
│   │   │   │   ├── ProductDTO.java
│   │   │   │   ├── ProductFeignDTO.java
│   │   │   │   └── ProductResponseWrapper.java
│   │   │   ├── feign/
│   │   │   │   └── ProductFeignClient.java
│   │   │   ├── exception/
│   │   │   │   ├── CartNotFoundException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── configuration/
│   │   │       └── OpenApiConfig.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```

### Key Annotations Used
- `@SpringBootApplication`: Main application class
- `@RestController`: REST controller
- `@Service`: Service layer component
- `@Repository`: Data access layer
- `@Document`: MongoDB document mapping
- `@Id`: MongoDB document ID
- `@FeignClient`: Feign client for inter-service communication
- `@EnableFeignClients`: Enable Feign clients
- `@EnableMongoRepositories`: Enable MongoDB repositories
- `@RestControllerAdvice`: Global exception handler
- `@ExceptionHandler`: Exception handling methods
- `@Valid`: Request validation
- `@Builder`: Lombok builder pattern

### Service Dependencies

The Cart Service depends on:
- **Product Service**: Required for fetching product details and prices
  - Endpoint: `GET /api/product/{id}`
  - Must be running before Cart Service operations

### Data Flow

1. **Add to Cart Flow**:
   ```
   Client → CartController → CartService → ProductFeignClient → Product Service
                                 ↓
                          CartRepository → MongoDB
                                 ↓
                          CartService → DTO Conversion → CartController → Client
   ```

2. **View Cart Flow**:
   ```
   Client → CartController → CartService → CartRepository → MongoDB
                                 ↓
                          CartService → DTO Conversion → CartController → Client
   ```

3. **Remove from Cart Flow**:
   ```
   Client → CartController → CartService → CartRepository → MongoDB
                                 ↓
                          CartService → DTO Conversion → CartController → Client
   ```

---

## Version Information

- **Application Version**: `0.0.1-SNAPSHOT`
- **Spring Boot Version**: `3.4.12`
- **Spring Cloud Version**: `2024.0.2`
- **Java Version**: `21`
- **Documentation Version**: `1.0`

---

*Last Updated: January 2025*

