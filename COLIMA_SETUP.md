# Using Colima + Docker CLI (Docker Desktop Alternative)

This guide shows how to run the Online Marketplace platform using Colima instead of Docker Desktop.

## What is Colima?

Colima is a lightweight, open-source container runtime for macOS (and Linux) that provides Docker-compatible CLI without Docker Desktop.

## Prerequisites Installation

### 1. Install Colima and Docker CLI

```bash
# Install Colima
brew install colima

# Install Docker CLI (without Docker Desktop)
brew install docker

# Install Docker Compose
brew install docker-compose

# Verify installations
colima --version
docker --version
docker-compose --version
```

### 2. Install Java and Maven

```bash
# Install Java 21
brew install openjdk@21

# Add to PATH (add to ~/.zshrc or ~/.bash_profile)
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Install Maven
brew install maven

# Verify
java -version
mvn -version
```

## Starting Colima

### Basic Startup

```bash
# Start Colima with default settings (2 CPUs, 2GB RAM)
colima start

# Or start with more resources (recommended for this project)
colima start --cpu 4 --memory 8
```

### Recommended Settings for This Project

```bash
# Start Colima with optimal settings for marketplace platform
colima start \
  --cpu 4 \
  --memory 8 \
  --disk 60 \
  --vm-type vz \
  --mount-type virtiofs
```

**Settings Explained:**
- `--cpu 4`: Allocates 4 CPU cores
- `--memory 8`: Allocates 8GB RAM
- `--disk 60`: Allocates 60GB disk space
- `--vm-type vz`: Uses macOS Virtualization.framework (faster)
- `--mount-type virtiofs`: Better file system performance

### Verify Colima is Running

```bash
# Check Colima status
colima status

# Test Docker
docker ps

# Should see: "CONTAINER ID   IMAGE   COMMAND   CREATED   STATUS   PORTS   NAMES"
```

## Project Setup with Colima

### 1. Navigate to Project

```bash
cd /Users/tri.abror/github/training-project-2025-11
```

### 2. Start Database Services

```bash
# Start all databases in background
docker-compose up -d

# Verify all containers are running
docker-compose ps

# You should see:
# - marketplace-postgres (port 5432)
# - marketplace-mongodb (port 27017)
# - marketplace-redis (port 6379)
# - marketplace-pgadmin (port 5050)
# - marketplace-mongo-express (port 8081)
# - marketplace-redis-commander (port 8082)
```

### 3. Check Container Logs (if needed)

```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs postgres
docker-compose logs mongodb
docker-compose logs redis

# Follow logs in real-time
docker-compose logs -f
```

### 4. Build Maven Project

```bash
# Build all services (skip tests for faster build)
mvn clean install -DskipTests

# Or build with tests
mvn clean install
```

### 5. Start All Services

You have two options: **Manual start** or **using a script**.

#### Option A: Manual Start (Recommended for learning)

Open **4 separate terminal windows**:

**Terminal 1 - API Gateway:**
```bash
cd /Users/tri.abror/github/training-project-2025-11/api-gateway
mvn spring-boot:run
```
Wait for: "Started GatewayApplication"

**Terminal 2 - Member Service:**
```bash
cd /Users/tri.abror/github/training-project-2025-11/member
mvn spring-boot:run
```
Wait for: "Started MemberApplication"

**Terminal 3 - Product Service:**
```bash
cd /Users/tri.abror/github/training-project-2025-11/product
mvn spring-boot:run
```
Wait for: "Started ProductApplication"

**Terminal 4 - Cart Service:**
```bash
cd /Users/tri.abror/github/training-project-2025-11/cart
mvn spring-boot:run
```
Wait for: "Started CartApplication"

#### Option B: Using Start Script

Create a startup script (see below).

## Testing the Setup

### 1. Check Database Connectivity

```bash
# PostgreSQL
docker exec -it marketplace-postgres psql -U marketplace -d marketplace_member -c "SELECT 1;"

# MongoDB
docker exec -it marketplace-mongodb mongosh -u marketplace -p marketplace_pass --authenticationDatabase admin --eval "db.adminCommand('ping')"

# Redis
docker exec -it marketplace-redis redis-cli -a marketplace_pass ping
```

### 2. Test API Endpoints

```bash
# Health check (API Gateway should be running)
curl http://localhost:8080/api/auth/login

# Should return error (no credentials) but confirms service is up
```

### 3. Complete Flow Test

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "colima@example.com",
    "username": "colimauser",
    "password": "Colima123!",
    "fullName": "Colima User"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "colima@example.com",
    "password": "Colima123!"
  }'
```

Save the token and test other endpoints!

## Enabling Data Seeding

### 1. Stop Services

Press `Ctrl+C` in each service terminal.

### 2. Enable Seeding in Configuration

**Member Service:**
```bash
# Edit member service config
vim /Users/tri.abror/github/training-project-2025-11/member/src/main/resources/application.yml

# Change:
data:
  seed:
    enabled: true  # Change from false
```

**Product Service:**
```bash
# Edit product service config
vim /Users/tri.abror/github/training-project-2025-11/product/src/main/resources/application.yml

# Change:
data:
  seed:
    enabled: true  # Change from false
```

### 3. Restart Services

Restart Member and Product services - they will auto-generate:
- 5,000 members (takes ~10-20 seconds)
- 50,000 products (takes ~2-3 minutes)

## Helpful Colima Commands

```bash
# Start Colima
colima start

# Stop Colima (keeps data)
colima stop

# Delete Colima VM (removes all data)
colima delete

# Check status
colima status

# SSH into Colima VM (for debugging)
colima ssh

# View resource usage
docker stats

# List all containers
docker ps -a

# Clean up stopped containers
docker system prune -a
```

## Docker Compose Commands

```bash
# Start services
docker-compose up -d

# Stop services (keeps data)
docker-compose stop

# Stop and remove containers (keeps volumes/data)
docker-compose down

# Stop and remove everything including data
docker-compose down -v

# View logs
docker-compose logs -f

# Restart specific service
docker-compose restart postgres

# Check service status
docker-compose ps
```

## Accessing Database Management Tools

All URLs work the same with Colima:

- **PgAdmin**: http://localhost:5050 (admin@marketplace.com / admin)
- **Mongo Express**: http://localhost:8081 (admin / admin)
- **Redis Commander**: http://localhost:8082

## Troubleshooting

### Issue: "Cannot connect to Docker daemon"

```bash
# Check if Colima is running
colima status

# If stopped, start it
colima start --cpu 4 --memory 8
```

### Issue: "Port already in use"

```bash
# Find process using port (e.g., 5432)
lsof -ti:5432 | xargs kill -9

# Or stop all Docker containers
docker-compose down
```

### Issue: "Connection refused to database"

```bash
# Restart all database containers
docker-compose restart

# Check if containers are healthy
docker-compose ps

# View container logs
docker-compose logs postgres
```

### Issue: Services can't connect to databases

```bash
# Check if using correct host (should be 'localhost')
# In application.yml files, database hosts should be:
# - PostgreSQL: localhost:5432
# - MongoDB: localhost:27017
# - Redis: localhost:6379
```

### Issue: Colima VM running out of disk space

```bash
# Stop Colima
colima stop

# Delete and recreate with more disk
colima delete
colima start --cpu 4 --memory 8 --disk 100
```

### Issue: Slow performance

```bash
# Restart Colima with more resources
colima stop
colima start --cpu 6 --memory 12 --vm-type vz --mount-type virtiofs
```

## Stopping Everything

### Stop Spring Boot Services
Press `Ctrl+C` in each terminal running a service.

### Stop Databases
```bash
# Stop containers but keep data
docker-compose stop

# Or stop and remove containers (data persists in volumes)
docker-compose down
```

### Stop Colima
```bash
# Stops the VM but keeps data
colima stop
```

### Complete Cleanup (Remove All Data)
```bash
# Stop all services
docker-compose down -v

# Stop and delete Colima VM
colima stop
colima delete
```

## Performance Comparison

**Colima vs Docker Desktop:**
- ✅ **Lighter**: Uses less RAM when idle (~500MB vs ~2GB)
- ✅ **Faster**: Better file system performance with virtiofs
- ✅ **Free**: No licensing requirements
- ✅ **Open Source**: Community-driven
- ⚠️ **No GUI**: Command-line only (but you have management UIs for databases)

## Next Steps

1. **Development Workflow**: Keep Colima running in background, start/stop services as needed
2. **Testing**: Use Postman collection for API testing
3. **Monitoring**: Use database management tools to inspect data
4. **Debugging**: Check service logs in terminal windows

## Resources

- Colima GitHub: https://github.com/abiosoft/colima
- Docker CLI Docs: https://docs.docker.com/engine/reference/commandline/cli/
- Docker Compose Docs: https://docs.docker.com/compose/

---

**You're all set!** 🚀 The platform runs identically on Colima as it would on Docker Desktop.
