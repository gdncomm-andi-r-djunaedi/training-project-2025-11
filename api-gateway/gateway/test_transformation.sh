#!/bin/bash
# Test OpenAPI Transformation

echo "============================================"
echo "Testing OpenAPI Server URL Transformation"
echo "============================================"
echo ""

# Make sure Gateway is running
if ! curl -s http://localhost:8070/actuator/health > /dev/null 2>&1; then
    echo "❌ Gateway is not running on port 8070"
    echo "Please start the Gateway first: mvn spring-boot:run"
    exit 1
fi

echo "✅ Gateway is running"
echo ""

echo "Testing Product Service API docs transformation..."
echo ""

# Fetch and check server URL
SERVER_URL=$(curl -s http://localhost:8070/api/v1/products/api-docs | jq -r '.servers[0].url')

echo "Server URL from transformed docs: $SERVER_URL"
echo ""

if [ "$SERVER_URL" = "http://localhost:8070" ]; then
    echo "✅ Transformation is CORRECT!"
    echo ""
    echo "Now test in Swagger:"
    echo "1. Open: http://localhost:8070/webjars/swagger-ui/index.html"
    echo "2. Select 'Product Service' from dropdown"
    echo "3. Try /api/v1/products/search"
    echo "4. Should call: http://localhost:8070/api/v1/products/search"
    echo ""
    echo "The URL should NOT be duplicated anymore!"
else
    echo "❌ Server URL is: $SERVER_URL"
    echo "Expected: http://localhost:8070"
    echo ""
    echo "Something is wrong with the transformation."
fi

echo ""
echo "============================================"

