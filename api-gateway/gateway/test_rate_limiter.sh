#!/bin/bash
# Rate Limiter Test Script
# This script tests the Gateway's rate limiting functionality

echo "============================================"
echo "Gateway Rate Limiter Test Script"
echo "============================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
GATEWAY_URL="http://localhost:8070"
TEST_ENDPOINT="/api/v1/auth/login"

# Step 1: Check Redis
echo "Step 1: Checking Redis..."
if redis-cli ping > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Redis is running${NC}"
else
    echo -e "${RED}✗ Redis is not running${NC}"
    echo "Start Redis with: redis-server"
    exit 1
fi

# Step 2: Check Gateway
echo ""
echo "Step 2: Checking Gateway..."
if curl -s "$GATEWAY_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Gateway is running${NC}"
else
    echo -e "${RED}✗ Gateway is not running${NC}"
    echo "Start Gateway with: mvn spring-boot:run"
    exit 1
fi

# Step 3: Clear existing rate limit data (optional)
echo ""
echo "Step 3: Clearing existing rate limit data..."
redis-cli --scan --pattern "request_rate_limiter*" | xargs -r redis-cli del > /dev/null 2>&1
echo -e "${GREEN}✓ Rate limit data cleared${NC}"

# Step 4: Test Rate Limiting
echo ""
echo "Step 4: Testing Rate Limiting..."
echo "Making 25 rapid requests to login endpoint (IP-based rate limiting)"
echo ""

SUCCESS_COUNT=0
RATE_LIMITED_COUNT=0
ERROR_COUNT=0

for i in {1..25}; do
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY_URL$TEST_ENDPOINT" \
        -H 'Content-Type: application/json' \
        -d '{"username":"testuser","password":"password"}' 2>/dev/null)
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
    
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "404" ]; then
        echo -e "Request $i: ${GREEN}✓ Allowed (HTTP $HTTP_CODE)${NC}"
        ((SUCCESS_COUNT++))
    elif [ "$HTTP_CODE" = "429" ]; then
        echo -e "Request $i: ${YELLOW}⚠ Rate Limited (HTTP 429)${NC}"
        ((RATE_LIMITED_COUNT++))
    else
        echo -e "Request $i: ${RED}✗ Error (HTTP $HTTP_CODE)${NC}"
        ((ERROR_COUNT++))
    fi
    
    # Small delay to see the pattern
    sleep 0.05
done

# Step 5: Summary
echo ""
echo "============================================"
echo "Test Results Summary"
echo "============================================"
echo -e "Total Requests:      25"
echo -e "${GREEN}Allowed:             $SUCCESS_COUNT${NC}"
echo -e "${YELLOW}Rate Limited (429):  $RATE_LIMITED_COUNT${NC}"
echo -e "${RED}Errors:              $ERROR_COUNT${NC}"
echo ""

if [ $RATE_LIMITED_COUNT -gt 0 ]; then
    echo -e "${GREEN}✓ Rate Limiting is WORKING!${NC}"
    echo "Some requests were rate limited as expected."
else
    echo -e "${YELLOW}⚠ No rate limiting detected${NC}"
    echo "Either the limits are very high, or rate limiting may not be configured properly."
fi

# Step 6: Check Redis Keys
echo ""
echo "============================================"
echo "Redis Rate Limit Keys"
echo "============================================"
KEYS=$(redis-cli --scan --pattern "request_rate_limiter*" | head -5)
if [ -n "$KEYS" ]; then
    echo "$KEYS"
    echo ""
    echo "Total keys: $(redis-cli --scan --pattern 'request_rate_limiter*' | wc -l)"
else
    echo "No rate limit keys found in Redis"
fi

echo ""
echo "============================================"
echo "Test Complete!"
echo "============================================"
echo ""
echo "To monitor in real-time, run:"
echo "  redis-cli MONITOR | grep request_rate_limiter"
echo ""
echo "To view all rate limit keys:"
echo "  redis-cli KEYS 'request_rate_limiter*'"
echo ""

