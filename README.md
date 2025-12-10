# Online Marketplace Platform

A microservices-based online marketplace platform built with Java, Spring Boot, and modern cloud technologies.

## Architecture

The system consists of 4 microservices:
- **API Gateway (Port 8080)**: Entry point for all requests, handles routing and authentication
- **Member Service (Port 8081)**: User registration, login, logout, and JWT token management
- **Product Service (Port 8082)**: Product catalog, search, and pagination
- **Cart Service (Port 8083)**: Shopping cart operations

## Tech Stack

- **Java 21** with Spring Boot 3.2.1
- **PostgreSQL**: Member data storage
- **MongoDB**: Product catalog storage
- **Redis**: Cart storage and token blacklist
- **JWT**: Authentication with HS256
- **Maven**: Build tool

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker and Docker Compose
  - **Option 1**: Docker Desktop (download from docker.com)
  - **Option 2**: Colima + Docker CLI (lightweight alternative)
    - See [COLIMA_SETUP.md](COLIMA_SETUP.md) for detailed Colima setup instructions

## Quick Start

### Option 1: Using Colima (Recommended for macOS)

**First Time Setup:**
```bash
# Install Colima and Docker CLI
brew install colima docker docker-compose

# Start Colima with recommended settings
colima start --cpu 4 --memory 8 --disk 60

# Verify
docker ps
```

**Then proceed to step 1 below**

### Option 2: Using Docker Desktop

Download and install Docker Desktop from docker.com, then proceed to step 1 below.

---

### 1. Start Infrastructure (Databases)

```bash
# Start PostgreSQL, MongoDB, and Redis
docker-compose up -d

# Verify all containers are running
docker-compose ps
```

### 2. Build All Services

```bash
# Build from root directory
mvn clean install
```

### 3. Start Services

**Option A: Automated (macOS only) - Recommended**
```bash
# This script opens all 4 services in separate terminal tabs
./start-all-services.sh
```

**Option B: Manual - Open 4 terminal windows and run each service:**

```bash
# Terminal 1: API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 2: Member Service
cd member && mvn spring-boot:run

# Terminal 3: Product Service
cd product && mvn spring-boot:run

# Terminal 4: Cart Service
cd cart && mvn spring-boot:run
```

Wait for each service to display "Started [Service]Application" message (~30-60 seconds total).

## Data Seeding

To seed the database with test data (5,000 members and 50,000 products):

1. Edit `member/src/main/resources/application.yml`:
   ```yaml
   data:
     seed:
       enabled: true
   ```

2. Edit `product/src/main/resources/application.yml`:
   ```yaml
   data:
     seed:
       enabled: true
   ```

3. Start the services - data will be generated on first run.

## API Documentation

All requests go through API Gateway at `http://localhost:8080`

### Authentication

#### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "testuser",
    "password": "Password123!",
    "fullName": "Test User"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "user@example.com",
    "password": "Password123!"
  }'
```

Response includes JWT token - use it in subsequent requests.

#### Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Products

#### Search Products
```bash
# Search with query
curl "http://localhost:8080/api/products/search?q=laptop&page=0&size=20"

# Get all products
curl "http://localhost:8080/api/products?page=0&size=20"
```

#### Get Product Detail
```bash
curl "http://localhost:8080/api/products/{product-id}"
```

### Shopping Cart (Requires Authentication)

#### Add to Cart
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "product-id",
    "productName": "Product Name",
    "price": 99.99,
    "quantity": 2
  }'
```

#### View Cart
```bash
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Remove from Cart
```bash
curl -X DELETE http://localhost:8080/api/cart/items/{product-id} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Testing

### Run All Tests
```bash
mvn clean test
```

### Run Integration Tests
```bash
mvn clean verify
```

### Run Tests for Specific Service
```bash
cd member && mvn test
cd product && mvn test
cd cart && mvn test
```

## Test Credentials

When data seeding is enabled, these test accounts are available:

- **Email**: `test@example.com`, **Password**: `Password123!`
- **Email**: `admin@marketplace.com`, **Password**: `Password123!`
- **Email**: `john@example.com`, **Password**: `Password123!`

## Database Access

### PostgreSQL (Member Service)
- **Connection**: `jdbc:postgresql://localhost:5432/marketplace_member`
- **Username**: `marketplace`
- **Password**: `marketplace_pass`
- **PgAdmin**: http://localhost:5050 (admin@marketplace.com / admin)

### MongoDB (Product Service)
- **Connection**: `mongodb://localhost:27017`
- **Username**: `marketplace`
- **Password**: `marketplace_pass`
- **Mongo Express**: http://localhost:8081 (admin / admin)

### Redis (Cart & Token Blacklist)
- **Connection**: `localhost:6379`
- **Password**: `marketplace_pass`
- **Redis Commander**: http://localhost:8082

## Project Structure

```
.
├── api-gateway/          # API Gateway service
├── member/              # Member authentication service
├── product/             # Product catalog service
├── cart/                # Shopping cart service
├── docker-compose.yml   # Infrastructure setup
└── pom.xml             # Parent POM
```

## Development Notes

### Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character (@$!%*?&)

### JWT Token
- Default expiration: 24 hours
- Stored in Redis blacklist after logout
- Can be passed via Authorization header or Cookie

### Performance Considerations
- Product search results are cached in Redis (10-minute TTL)
- Shopping carts stored in Redis with 24-hour TTL
- Batch inserts used for data seeding

## Troubleshooting

### Port Already in Use
```bash
# Find and kill process on port
lsof -ti:8080 | xargs kill -9
```

### Database Connection Issues
```bash
# Restart Docker containers
docker-compose restart

# Check container logs
docker-compose logs postgres
docker-compose logs mongodb
docker-compose logs redis
```

### Clear All Data
```bash
# Stop and remove containers with volumes
docker-compose down -v

# Restart fresh
docker-compose up -d
```

## License

This project is licensed under the MIT License.
