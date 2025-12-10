# 🚀 Quick Start with Colima

Complete setup in 5 minutes!

## 1️⃣ Install Everything (One Time)

```bash
# Install all tools at once
brew install colima docker docker-compose openjdk@21 maven

# Configure Java path
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

## 2️⃣ Start Colima

```bash
# Start with recommended settings (takes ~30 seconds first time)
colima start --cpu 4 --memory 8 --disk 60

# Verify it's running
docker ps
# Should show: CONTAINER ID   IMAGE   COMMAND   CREATED   STATUS   PORTS   NAMES
```

## 3️⃣ Start Databases

```bash
cd /Users/tri.abror/github/training-project-2025-11

# Start PostgreSQL, MongoDB, Redis
docker-compose up -d

# Wait for health checks (~15 seconds)
sleep 15

# Verify all running
docker-compose ps
```

## 4️⃣ Build Project

```bash
# Build all services (takes ~2 minutes first time)
mvn clean install -DskipTests
```

## 5️⃣ Start All Services

**Automated (Recommended):**
```bash
# Opens 4 terminal tabs automatically
./start-all-services.sh
```

**Manual:**
Open 4 terminals and run:
- Terminal 1: `cd api-gateway && mvn spring-boot:run`
- Terminal 2: `cd member && mvn spring-boot:run`
- Terminal 3: `cd product && mvn spring-boot:run`
- Terminal 4: `cd cart && mvn spring-boot:run`

Wait for "Started [Service]Application" in each terminal (~60 seconds total).

## 6️⃣ Test It Works!

```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "username": "testuser",
    "password": "Password123!",
    "fullName": "Test User"
  }'

# Should return: { "token": "...", "member": {...} }
```

## ✅ You're Done!

**Services Running:**
- API Gateway: http://localhost:8080
- Member Service: http://localhost:8081  
- Product Service: http://localhost:8082
- Cart Service: http://localhost:8083

**Database Admin Tools:**
- PgAdmin: http://localhost:5050
- Mongo Express: http://localhost:8081
- Redis Commander: http://localhost:8082

## 🛑 Stopping Everything

**Stop Services:** Press `Ctrl+C` in each terminal

**Stop Databases:**
```bash
docker-compose stop  # Keeps data
# or
docker-compose down  # Removes containers, keeps data
```

**Stop Colima:**
```bash
colima stop  # Keeps VM and data
```

## 🔄 Daily Workflow

**Morning (Start):**
```bash
colima start
docker-compose up -d
./start-all-services.sh
```

**Evening (Stop):**
```bash
# Ctrl+C in service terminals
docker-compose stop
colima stop
```

## 📚 Full Documentation

- Detailed Colima Guide: [COLIMA_SETUP.md](COLIMA_SETUP.md)
- Complete README: [README.md](README.md)
- Step-by-Step Guide: [GETTING_STARTED.md](GETTING_STARTED.md)
- API Testing: Import [postman_collection.json](postman_collection.json)

## 💡 Helpful Commands

```bash
# Check Colima status
colima status

# View running containers
docker ps

# View database logs
docker-compose logs -f postgres

# Restart databases
docker-compose restart

# Clean everything (nuclear option)
docker-compose down -v && colima stop && colima delete
```

## ⚠️ Common Issues

**"Cannot connect to Docker daemon"**
```bash
colima start --cpu 4 --memory 8
```

**Database connection errors**
```bash
docker-compose restart
docker-compose ps  # Check health
```

**Port conflicts**
```bash
lsof -ti:8080 | xargs kill -9  # Kill process on port
```

---

**Need Help?** Check [COLIMA_SETUP.md](COLIMA_SETUP.md) for detailed troubleshooting!
