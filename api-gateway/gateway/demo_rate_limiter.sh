#!/bin/bash
# Simple Rate Limiter Demo
# Shows rate limiting in action with color-coded output

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

GATEWAY_URL="http://localhost:8070"

clear
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         Gateway Rate Limiter - Live Demo                  ║${NC}"
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo ""

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"
if ! redis-cli ping > /dev/null 2>&1; then
    echo -e "${RED}✗ Redis is not running. Please start Redis first.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Redis is running${NC}"

if ! curl -s "$GATEWAY_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}✗ Gateway is not running. Please start Gateway first.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Gateway is running${NC}"
echo ""

# Clean slate
echo -e "${YELLOW}Clearing old rate limit data...${NC}"
redis-cli --scan --pattern "request_rate_limiter*" | xargs -r redis-cli del > /dev/null 2>&1
echo -e "${GREEN}✓ Ready for testing${NC}"
echo ""

echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Test: Making 30 rapid requests to login endpoint${NC}"
echo -e "${BLUE}Expected: First ~20 succeed, rest get rate limited (429)${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""

sleep 2

SUCCESS=0
RATE_LIMITED=0

for i in {1..30}; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST "$GATEWAY_URL/api/v1/auth/login" \
        -H 'Content-Type: application/json' \
        -d '{"username":"test","password":"test"}')
    
    if [ "$HTTP_CODE" = "429" ]; then
        echo -e "Request ${i}/30: ${RED}✗ RATE LIMITED${NC} (HTTP 429)"
        ((RATE_LIMITED++))
    else
        echo -e "Request ${i}/30: ${GREEN}✓ ALLOWED${NC} (HTTP $HTTP_CODE)"
        ((SUCCESS++))
    fi
    
    sleep 0.1
done

echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Results:${NC}"
echo -e "${GREEN}  Allowed:       $SUCCESS${NC}"
echo -e "${RED}  Rate Limited:  $RATE_LIMITED${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""

if [ $RATE_LIMITED -gt 0 ]; then
    echo -e "${GREEN}✓ SUCCESS! Rate limiting is working correctly!${NC}"
    echo ""
    echo "Rate limit keys in Redis:"
    redis-cli --scan --pattern "request_rate_limiter*" | head -5
else
    echo -e "${YELLOW}⚠ WARNING: No requests were rate limited${NC}"
    echo "This might mean:"
    echo "  - Rate limits are very high"
    echo "  - Rate limiting is not configured"
    echo "  - Check application.properties configuration"
fi

echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "To see detailed rate limit error response, run:"
echo -e "${YELLOW}curl -X POST $GATEWAY_URL/api/v1/auth/login \\${NC}"
echo -e "${YELLOW}  -H 'Content-Type: application/json' \\${NC}"
echo -e "${YELLOW}  -d '{\"username\":\"test\",\"password\":\"test\"}'${NC}"
echo ""
echo "To monitor Redis in real-time:"
echo -e "${YELLOW}redis-cli MONITOR | grep request_rate_limiter${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"

