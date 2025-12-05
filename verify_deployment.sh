#!/bin/bash

BASE_URL="http://localhost:8080/api"

echo "Waiting for services to be ready..."
sleep 10

echo "1. Registering User..."
curl -v -X POST $BASE_URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com", "password": "password123"}'
echo -e "\n"

echo "2. Logging in..."
LOGIN_RESPONSE=$(curl -v -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}')
echo "Login Response: $LOGIN_RESPONSE"
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token: $TOKEN"
echo -e "\n"

if [ -z "$TOKEN" ]; then
  echo "Failed to get token. Exiting."
  exit 1
fi

echo "3. Searching Products..."
curl -v -X GET "$BASE_URL/products?search=phone" \
  -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "4. Adding to Cart..."
# First get a product ID
PRODUCT_ID=$(curl -s -X GET "$BASE_URL/products?size=1" -H "Authorization: Bearer $TOKEN" | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)
echo "Product ID: $PRODUCT_ID"

if [ -z "$PRODUCT_ID" ]; then
  echo "Failed to get product ID. Exiting."
  exit 1
fi

curl -v -X POST "$BASE_URL/cart/1/items?productId=$PRODUCT_ID&quantity=1" \
  -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "5. Viewing Cart..."
curl -v -X GET "$BASE_URL/cart/1" \
  -H "Authorization: Bearer $TOKEN"
echo -e "\n"
