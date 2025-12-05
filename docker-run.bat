@echo off
REM Docker run script for Windows
REM This script builds and starts all microservices using Docker Compose

echo ========================================
echo Marketplace Microservices - Docker
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running. Please start Docker Desktop and try again.
    pause
    exit /b 1
)

REM Parse command line arguments
set MODE=up
set BUILD_FLAG=--build

if "%1"=="down" (
    echo Stopping all services...
    docker-compose down
    echo.
    echo All services stopped.
    pause
    exit /b 0
)

if "%1"=="logs" (
    echo Showing logs for all services...
    docker-compose logs -f
    exit /b 0
)

if "%1"=="rebuild" (
    echo Rebuilding all services...
    docker-compose down
    docker-compose build --no-cache
    set BUILD_FLAG=
)

if "%1"=="nobuild" (
    set BUILD_FLAG=
)

REM Start services
echo Building and starting all services...
echo This may take a few minutes on first run...
echo.

docker-compose up %BUILD_FLAG% -d

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start services. Check the logs above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Services started successfully!
echo ========================================
echo.
echo API Gateway:     http://localhost:8080
echo Member Service:  http://localhost:8081
echo Product Service: http://localhost:8082
echo Cart Service:    http://localhost:8083
echo.
echo PostgreSQL:      localhost:5432
echo MongoDB:         localhost:27017
echo Redis:           localhost:6379
echo.
echo To view logs: docker-compose logs -f
echo To stop:      docker-compose down
echo.
echo Checking service health...
timeout /t 5 /nobreak >nul
docker-compose ps
echo.
pause
