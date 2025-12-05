#!/bin/bash
# Swagger Debugging Script for Gateway

echo "=========================================="
echo "Gateway Swagger Debug - Service Status"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Test 1: Check if services are running
echo -e "${BLUE}Step 1: Checking if services are running...${NC}"
echo ""

echo -n "Gateway (8070): "
if curl -s http://localhost:8070/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Running${NC}"
else
    echo -e "${RED}✗ Not running${NC}"
fi

echo -n "Member Service (8061): "
if curl -s http://localhost:8061/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Running${NC}"
    MEMBER_RUNNING=true
else
    echo -e "${RED}✗ Not running${NC}"
    MEMBER_RUNNING=false
fi

echo -n "Product Service (8062): "
if curl -s http://localhost:8062/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Running${NC}"
    PRODUCT_RUNNING=true
else
    echo -e "${RED}✗ Not running${NC}"
    PRODUCT_RUNNING=false
fi

echo -n "Cart Service (8063): "
if curl -s http://localhost:8063/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Running${NC}"
    CART_RUNNING=true
else
    echo -e "${RED}✗ Not running${NC}"
    CART_RUNNING=false
fi

echo ""
echo -e "${BLUE}Step 2: Finding correct API docs paths...${NC}"
echo ""

# Test Member Service
if [ "$MEMBER_RUNNING" = true ]; then
    echo "Member Service - Testing API docs paths:"
    
    if curl -s http://localhost:8061/v3/api-docs | grep -q "openapi"; then
        echo -e "  ${GREEN}✓ /v3/api-docs${NC} (works)"
        MEMBER_PATH="/v3/api-docs"
    else
        echo -e "  ${RED}✗ /v3/api-docs${NC} (not found)"
    fi
    
    if curl -s http://localhost:8061/api/v3/api-docs | grep -q "openapi"; then
        echo -e "  ${GREEN}✓ /api/v3/api-docs${NC} (works)"
        MEMBER_PATH="/api/v3/api-docs"
    else
        echo -e "  ${RED}✗ /api/v3/api-docs${NC} (not found)"
    fi
    
    if curl -s http://localhost:8061/api-docs | grep -q "openapi"; then
        echo -e "  ${GREEN}✓ /api-docs${NC} (works)"
        MEMBER_PATH="/api-docs"
    else
        echo -e "  ${RED}✗ /api-docs${NC} (not found)"
    fi
fi

echo ""

# Test Product Service  
if [ "$PRODUCT_RUNNING" = true ]; then
    echo "Product Service - Testing API docs paths:"
    
    if curl -s http://localhost:8062/v3/api-docs | grep -q "openapi"; then
        echo -e "  ${GREEN}✓ /v3/api-docs${NC} (works)"
        PRODUCT_PATH="/v3/api-docs"
    else
        echo -e "  ${RED}✗ /v3/api-docs${NC} (not found)"
    fi
    
    if curl -s http://localhost:8062/api/v3/api-docs | grep -q "openapi"; then
        echo -e "  ${GREEN}✓ /api/v3/api-docs${NC} (works)"
        PRODUCT_PATH="/api/v3/api-docs"
    else
        echo -e "  ${RED}✗ /api/v3/api-docs${NC} (not found)"
    fi
    
    if curl -s http://localhost:8062/api-docs | grep -q "openapi"; then
        echo -e "  ${GREEN}✓ /api-docs${NC} (works)"
        PRODUCT_PATH="/api-docs"
    else
        echo -e "  ${RED}✗ /api-docs${NC} (not found)"
    fi
fi

echo ""
echo -e "${BLUE}Step 3: Testing through Gateway routes...${NC}"
echo ""

# Test Member Service through Gateway
if [ "$MEMBER_RUNNING" = true ]; then
    echo -n "Member Service via Gateway: "
    if curl -s http://localhost:8070/api/v1/members/v3/api-docs | grep -q "openapi"; then
        echo -e "${GREEN}✓ Works${NC}"
    else
        echo -e "${RED}✗ Failed${NC}"
        echo "  Trying direct URL to see what's wrong..."
        curl -s http://localhost:8070/api/v1/members/v3/api-docs | head -5
    fi
fi

# Test Product Service through Gateway
if [ "$PRODUCT_RUNNING" = true ]; then
    echo -n "Product Service via Gateway: "
    if curl -s http://localhost:8070/api/v1/products/api-docs | grep -q "openapi"; then
        echo -e "${GREEN}✓ Works${NC}"
    else
        echo -e "${RED}✗ Failed${NC}"
        echo "  Trying direct URL to see what's wrong..."
        curl -s http://localhost:8070/api/v1/products/api-docs | head -5
    fi
fi

echo ""
echo -e "${BLUE}Step 4: Recommendations${NC}"
echo ""

if [ "$MEMBER_RUNNING" = true ] && [ -n "$MEMBER_PATH" ]; then
    if [ "$MEMBER_PATH" != "/v3/api-docs" ]; then
        echo -e "${YELLOW}⚠ Member Service uses $MEMBER_PATH instead of /v3/api-docs${NC}"
        echo "  Update application.properties line 25:"
        echo "  spring.cloud.gateway.routes[1].filters[0]=RewritePath=/api/v1/members/v3/api-docs,$MEMBER_PATH"
    else
        echo -e "${GREEN}✓ Member Service path is correct${NC}"
    fi
fi

if [ "$PRODUCT_RUNNING" = true ] && [ -n "$PRODUCT_PATH" ]; then
    if [ "$PRODUCT_PATH" != "/api-docs" ]; then
        echo -e "${YELLOW}⚠ Product Service uses $PRODUCT_PATH instead of /api-docs${NC}"
        echo "  Update application.properties line 31:"
        echo "  spring.cloud.gateway.routes[2].filters[0]=RewritePath=/api/v1/products/api-docs,$PRODUCT_PATH"
    else
        echo -e "${GREEN}✓ Product Service path is correct${NC}"
    fi
fi

echo ""
echo "=========================================="
echo "Debug Complete!"
echo "=========================================="

