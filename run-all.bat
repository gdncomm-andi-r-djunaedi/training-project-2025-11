@echo off
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"
start "Member Service" cmd /k "cd member && mvn spring-boot:run"
start "Product Service" cmd /k "cd product && mvn spring-boot:run"
start "Cart Service" cmd /k "cd cart && mvn spring-boot:run"
