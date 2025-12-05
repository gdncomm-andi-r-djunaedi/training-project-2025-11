#!/bin/bash

GATEWAY_URL="http://localhost:8070"
EMAIL="test@example.com"
PASSWORD="Test@123456"

echo "=== Testing Member Endpoints via Gateway ==="
echo ""

# 1. Register
echo "1. Registering user..."
REGISTER_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/v1/members/register" \
  -H 'Content-Type: application/json' \
  -d "{
    \"email\": \"$EMAIL\",
    \"password\": \"$PASSWORD\"
  }")

echo "Register Response:"
echo "$REGISTER_RESPONSE" | jq '.' 2>/dev/null || echo "$REGISTER_RESPONSE"
echo ""

# 2. Login
echo "2. Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/v1/members/login" \
  -H 'Content-Type: application/json' \
  -d "{
    \"username\": \"$EMAIL\",
    \"password\": \"$PASSWORD\"
  }")

echo "Login Response:"
echo "$LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGIN_RESPONSE"
echo ""

# Extract token (if jq is available)
if command -v jq &> /dev/null; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken // empty')
    if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        echo "Access Token extracted: ${TOKEN:0:50}..."
        echo ""
        
        # 3. Logout
        echo "3. Logging out..."
        LOGOUT_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/v1/members/logout" \
          -H "Authorization: Bearer $TOKEN" \
          -H 'Content-Type: application/json')
        
        echo "Logout Response:"
        echo "$LOGOUT_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGOUT_RESPONSE"
    else
        echo "Could not extract token from login response"
    fi
else
    echo "jq not installed. Install it to extract token and test logout."
    echo "On macOS: brew install jq"
fi

echo ""
echo "=== Testing Complete ==="
