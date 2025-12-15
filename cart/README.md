# 🛒 E-Commerce Shopping Cart System Design

Based on your requirements and industry best practices, here's a comprehensive system design for a shopping cart service:

## 📊 **System Architecture Overview**

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                         │
│                    (Web/Mobile Apps)                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway                            │
│              (Authentication/Rate Limiting)                 │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
        ▼                         ▼
┌──────────────────┐      ┌──────────────────┐
│   Cart Service   │      │ Product Service  │
│  (Spring Boot)   │──────│  (Your Existing) │
└────────┬─────────┘      └──────────────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌───────┐ ┌──────────┐
│ Redis │ │ MongoDB  │
│(Cache)│ │(Persist) │
└───────┘ └──────────┘
```

## 🎯 **Recommended Architecture: Hybrid Approach**

### **Strategy: Redis (Primary) + MongoDB (Backup)**

Using Redis for shopping cart storage provides fast read and write performance, reducing request size and improving user experience. Here's why this approach works best:

### **1. Redis (Primary Storage)**
- **Use Case**: Active shopping carts (hot data)
- **Data Structure**: Hash
- **TTL**: 7-30 days
- **Why**: Redis provides speed, scale, availability, and cost efficiency essential for session stores, storing data as key-value pairs with user identifiers as keys

### **2. MongoDB (Persistent Storage)**
- **Use Case**: Abandoned carts, historical data, analytics
- **When**: Sync periodically or on specific events
- **Why**: Long-term retention, recovery, and analytics

## 🗄️ **Database Schema Design**

### **Redis Data Structure**

```
Key Pattern: cart:{userId}
Data Type: Hash
Fields: product:{productId} → {quantity}

Example:
HSET cart:user123 product:prod001 2
HSET cart:user123 product:prod002 1
```

**Redis Commands:**
```redis
# Add/Update item
HSET cart:user123 product:prod001 2

# Get entire cart
HGETALL cart:user123

# Remove item
HDEL cart:user123 product:prod001

# Delete cart
DEL cart:user123

# Set expiration (7 days)
EXPIRE cart:user123 604800
```

### **MongoDB Schema**

```javascript
CARTS {
    _id: ObjectId,
    user_id: String (indexed, required),
    items: [
        {
            product_id: String (required),
            product_sku: String,
            product_name: String,
            product_image: String,
            quantity: Integer (required, min: 1),
            price: Decimal128 (snapshot price),
            added_at: Timestamp
        }
    ],
    total_items: Integer,
    total_amount: Decimal128,
    status: String (active/abandoned/converted),
    session_id: String (optional),
    created_at: Timestamp,
    updated_at: Timestamp,
    expires_at: Timestamp (indexed),
    converted_to_order_id: String (optional),
    metadata: {
        ip_address: String,
        user_agent: String,
        last_activity: Timestamp
    }
}
```

**Indexes:**
```javascript
{
    "user_id": 1,
    "status": 1
}
{
    "expires_at": 1
} // For TTL cleanup
{
    "session_id": 1
} // For anonymous carts
```

## 🏗️ **Technology Stack**

### **Backend**
- **Framework**: Spring Boot 3.x (Java 21)
- **API**: REST (Spring Web)
- **Documentation**: Swagger/OpenAPI
- **Authentication**: Spring Security + JWT
- **Validation**: Jakarta Validation

### **Databases**
- **Primary Cache**: Redis 6+ (shopping cart data)
- **Persistent DB**: MongoDB 4.4+ (backup, analytics)
- **Product Data**: Your existing Product Service

### **Additional Components**
- **Message Queue**: RabbitMQ/Kafka (optional, for async operations)
- **Monitoring**: Spring Actuator + Prometheus
- **Logging**: SLF4J + Logback

## 🔄 **Data Flow & Synchronization Strategy**

### **Write Operations:**

```
1. User adds item to cart
   ↓
2. Write to Redis immediately (fast response)
   ↓
3. Async event triggers MongoDB sync (eventual consistency)
   ↓
4. Return success to user
```

### **Read Operations:**

```
1. User requests cart
   ↓
2. Check Redis first (cache hit)
   ↓
3. If not found, check MongoDB (cache miss)
   ↓
4. If found in MongoDB, populate Redis
   ↓
5. Return cart data
```

### **Sync Strategies:**

**Option A: Event-Driven (Recommended)**
```
Cart Modified → Publish Event → Async Handler → Update MongoDB
```

**Option B: Scheduled Sync**
```
Every 5 minutes → Scan modified carts in Redis → Bulk update MongoDB
```

**Option C: Write-Through**
```
Cart Modified → Update Redis → Update MongoDB (synchronous)
```

## 📝 **API Design**

### **Authentication Required**
All endpoints require JWT token in Authorization header

### **Endpoints:**

```
POST   /api/v1/cart/items              # Add item to cart
PUT    /api/v1/cart/items/{productId}  # Update item quantity
DELETE /api/v1/cart/items/{productId}  # Remove item from cart
GET    /api/v1/cart                    # Get user's cart
DELETE /api/v1/cart                    # Clear entire cart
POST   /api/v1/cart/merge              # Merge guest cart (after login)
GET    /api/v1/cart/count               # Get item count
```

### **Request/Response Examples:**

**Add Item to Cart:**
```json
POST /api/v1/cart/items
{
  "product_id": "507f1f77bcf86cd799439011",
  "quantity": 2
}

Response:
{
  "cart_id": "user123",
  "items": [
    {
      "product_id": "507f1f77bcf86cd799439011",
      "product_sku": "PROD-001",
      "product_name": "Premium Wireless Headphones",
      "product_image": "https://...",
      "quantity": 2,
      "price": 299.99,
      "subtotal": 599.98
    }
  ],
  "total_items": 2,
  "total_amount": 599.98,
  "updated_at": "2024-12-04T10:30:00Z"
}
```

**Get Cart:**
```json
GET /api/v1/cart

Response:
{
  "cart_id": "user123",
  "user_id": "user123",
  "items": [...],
  "total_items": 5,
  "total_amount": 1499.95,
  "created_at": "2024-12-03T15:20:00Z",
  "updated_at": "2024-12-04T10:30:00Z"
}
```

## 🔐 **Security Considerations**

1. **Authentication**: JWT tokens with user ID claim
2. **Authorization**: Users can only access their own carts
3. **Validation**:
    - Maximum quantity per item (e.g., 99)
    - Maximum items in cart (e.g., 100)
    - Product existence validation
4. **Rate Limiting**: Prevent abuse (e.g., 100 requests/minute per user)

## ⚡ **Performance Optimizations**

### **1. Redis Configuration**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

### **2. Caching Strategy**
- Cart data in Redis with 7-day TTL
- Product details cached separately (using your Product Service cache)
- User session data linked to cart

### **3. Async Processing**
```java
@Async
public void syncCartToMongoDB(String userId, Cart cart) {
    // Async MongoDB sync
}
```

### **4. Batch Operations**
```java
// Get multiple products at once
List<Product> products = productService.getProductsByIds(productIds);
```

## 📊 **Data Management**

### **Cart Lifecycle:**

1. **Active**: User actively shopping (Redis + MongoDB)
2. **Abandoned**: No activity for 24 hours (MongoDB only, Redis expired)
3. **Converted**: Converted to order (MongoDB archived)

### **Cleanup Strategy:**

```java
@Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
public void cleanupExpiredCarts() {
    // Remove MongoDB carts older than 30 days
    cartRepository.deleteExpiredCarts(
        LocalDateTime.now().minusDays(30)
    );
}
```

### **Cart Recovery:**
- User logs in → Check Redis → If not found → Load from MongoDB → Populate Redis
- Guest cart → User logs in → Merge guest cart with user cart

## 🎯 **Best Practices**

### **1. Product Data Handling**
Store product snapshots (name, price, image) in cart to handle price changes and product deletions gracefully

### **2. Error Handling**
```java
@ControllerAdvice
public class CartExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<?> handleProductNotFound() {
        // Return meaningful error
    }
}
```

### **3. Monitoring**
- Track cart abandonment rate
- Monitor Redis memory usage
- Alert on sync failures
- Log suspicious activities

### **4. Testing**
- Unit tests for business logic
- Integration tests with Redis/MongoDB
- Load tests for concurrent users
- Redis failover tests

## 🔄 **Scalability Considerations**

### **Horizontal Scaling:**
- Stateless Cart Service (multiple instances)
- Redis Cluster for distributed caching
- MongoDB Replica Set for high availability

### **Vertical Scaling:**
- Redis memory optimization
- MongoDB sharding by user_id

## 📈 **Analytics & Insights**

Store in MongoDB for analysis:
- Cart abandonment patterns
- Popular product combinations
- Average cart value
- Time to purchase
- Conversion funnels

## 🚀 **Implementation Priority**

**Phase 1: MVP**
- Basic cart operations (add, view, remove)
- Redis storage
- JWT authentication
- Product validation

**Phase 2: Enhanced**
- MongoDB persistence
- Cart recovery
- Analytics events
- Guest cart support

**Phase 3: Advanced**
- Cart abandonment emails
- Product recommendations
- Real-time sync
- Advanced analytics

Would you like me to provide the complete Spring Boot implementation code for this cart system? I can create all the necessary controllers, services, entities, and configurations following the same clean architecture as your Product Service! 🚀