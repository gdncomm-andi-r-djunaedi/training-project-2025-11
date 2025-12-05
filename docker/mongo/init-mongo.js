// Create marketplace_product database and user
db = db.getSiblingDB('marketplace_product');

// Create indexes for products collection
db.createCollection('products');

db.products.createIndex({ "name": "text", "description": "text" }, { weights: { name: 10, description: 5 } });
db.products.createIndex({ "category": 1 });
db.products.createIndex({ "brand": 1 });
db.products.createIndex({ "active": 1 });
db.products.createIndex({ "price": 1 });
db.products.createIndex({ "createdAt": -1 });

print('MongoDB initialized with marketplace_product database and indexes');

