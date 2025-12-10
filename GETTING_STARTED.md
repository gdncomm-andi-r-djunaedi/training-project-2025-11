# Getting Started Guide

## Prerequisites Installation

### 1. Install Java 21
```bash
# macOS (using Homebrew)
brew install openjdk@21

# Verify installation
java -version
```

### 2. Install Maven
```bash
# macOS (using Homebrew)
brew install maven

# Verify installation
mvn -version
```

### 3. Install Docker Desktop
Download and install from: https://www.docker.com/products/docker-desktop

**Alternative**: Use Colima (lightweight, free alternative)
- See [COLIMA_SETUP.md](COLIMA_SETUP.md) for complete Colima setup instructions
- Recommended for macOS users who prefer command-line tools

## Project Setup

### 1. Clone and Navigate to Project
```bash
cd /Users/tri.abror/github/training-project-2025-11
```

### 2. Start Database Infrastructure
```bash
# Start all databases in background
docker-compose up -d

# Verify all containers are running (should see postgres, mongodb, redis)
docker-compose ps

# View logs if needed
docker-compose logs -f
```

### 3. Build All Microservices
```bash
# Build all services from root
mvn clean install -DskipTests

# This will take a few minutes on first run
```

### 4. Start Services

Open 4 terminal windows and run each command:

**Terminal 1 - API Gateway:**
```bash
cd api-gateway
mvn spring-boot:run
```
Wait for: "Started GatewayApplication in X seconds"

**Terminal 2 - Member Service:**
```bash
cd member
mvn spring-boot:run
```
Wait for: "Started MemberApplication in X seconds"

**Terminal 3 - Product Service:**
```bash
cd product
mvn spring-boot:run
```
Wait for: "Started ProductApplication in X seconds"

**Terminal 4 - Cart Service:**
```bash
cd cart
mvn spring-boot:run
```
Wait for: "Started CartApplication in X seconds"

## Testing the Platform

### 1. Enable Data Seeding (First Time Only)

**For Member Service:**
Edit `member/src/main/resources/application.yml`:
```yaml
data:
  seed:
    enabled: true  # Change from false to true
```

**For Product Service:**
Edit `product/src/main/resources/application.yml`:
```yaml
data:
  seed:
    enabled: true  # Change from false to true
```

Restart Member and Product services to generate data.

### 2. Test Complete Flow

**Step 1: Register a new user**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@example.com",
    "username": "demouser",
    "password": "Demo123!@#",
    "fullName": "Demo User"
  }'
```

**Step 2: Login (save the token from response)**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "demo@example.com",
    "password": "Demo123!@#"
  }'
```

Copy the `token` value from the response. You'll use it as `YOUR_TOKEN` below.

**Step 3: Search for products**
```bash
curl "http://localhost:8080/api/products/search?q=phone&page=0&size=5"
```

Copy a `productId` from the response.

**Step 4: Add product to cart**
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PRODUCT_ID_FROM_SEARCH",
    "productName": "Product Name",
    "price": 99.99,
    "quantity": 2
  }'
```

**Step 5: View your cart**
```bash
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Step 6: Logout**
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Accessing Database Management Tools

### PostgreSQL (Member Data)
- **PgAdmin**: http://localhost:5050
- **Login**: admin@marketplace.com / admin
- **Add Server**: 
  - Host: postgres (container name)
  - Port: 5432
  - Database: marketplace_member
  - Username: marketplace
  - Password: marketplace_pass

### MongoDB (Product Data)
- **Mongo Express**: http://localhost:8081
- **Login**: admin / admin

### Redis (Cart & Tokens)
- **Redis Commander**: http://localhost:8082

## Common Issues

### "Port already in use"
```bash
# Find process using port 8080 (or 8081, 8082, 8083)
lsof -ti:8080 | xargs kill -9
```

### "Connection refused" errors
```bash
# Restart all databases
docker-compose restart

# Check if containers are running
docker-compose ps
```

### Database not initialized
```bash
# Stop and remove everything
docker-compose down -v

# Start fresh
docker-compose up -d
```

## Next Steps

1. **Run Tests**: `mvn test` in each service directory
2. **Check Logs**: Monitor service logs for errors
3. **Explore APIs**: Use the test credentials to explore functionality
4. **Review Code**: Start with the implementation plan document

## Stopping the Platform

```bash
# Stop all Spring Boot services: Ctrl+C in each terminal

# Stop databases
docker-compose down

# Stop and remove all data
docker-compose down -v
```
