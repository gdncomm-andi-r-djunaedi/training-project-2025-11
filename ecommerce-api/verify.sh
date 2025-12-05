#!/bin/bash
# Register
echo "Registering..."
curl -s -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
echo ""

# Login
echo "Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"testuser","password":"password123"}')
echo "Login Response: $LOGIN_RESPONSE"
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | grep -o '[^"]*$')
echo "Token: $TOKEN"

# List Products
echo "Listing Products..."
curl -s -X GET "http://localhost:8080/api/products?size=1" -H "Authorization: Bearer $TOKEN"
echo ""

# Get Product ID (assuming ID 1 exists from DataInitializer)
PRODUCT_ID=1

# Add to Cart
echo "Adding to Cart..."
curl -s -X POST http://localhost:8080/api/cart/items -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d "{\"productId\":$PRODUCT_ID,\"quantity\":2}"
echo ""

# View Cart
echo "Viewing Cart..."
curl -s -X GET http://localhost:8080/api/cart -H "Authorization: Bearer $TOKEN"
echo ""

# Delete from Cart
echo "Deleting from Cart..."
curl -s -X DELETE http://localhost:8080/api/cart/items/1 -H "Authorization: Bearer $TOKEN"
echo ""

# View Cart again
echo "Viewing Cart after delete..."
curl -s -X GET http://localhost:8080/api/cart -H "Authorization: Bearer $TOKEN"
echo ""

# Logout
echo "Logging out..."
curl -s -X POST http://localhost:8080/api/auth/logout -H "Authorization: Bearer $TOKEN"
echo ""
