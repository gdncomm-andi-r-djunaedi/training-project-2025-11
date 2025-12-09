// Connect to admin database
db = db.getSiblingDB('admin');

// Create user only if not exists
if (!db.getUser("marketplace")) {
    db.createUser({
        user: "marketplace",
        pwd: "marketplace_pass",
        roles: [
            { role: "readWrite", db: "marketplace_product" },
            { role: "dbAdmin", db: "marketplace_product" }
        ]
    });
    print("✔ User 'marketplace' created.");
} else {
    print("ℹ User 'marketplace' already exists, skipping creation.");
}

// Switch to marketplace_product DB
db = db.getSiblingDB('marketplace_product');

// Create collection safely
if (!db.getCollectionNames().includes("products")) {
    db.createCollection("products", {
        validator: {
            $jsonSchema: {
                bsonType: "object",
                required: ["name", "price", "category"],
                properties: {
                    name: { bsonType: "string" },
                    description: { bsonType: "string" },
                    category: { bsonType: "string" },
                    price: { bsonType: "double", minimum: 0 },
                    imageUrl: { bsonType: "string" },
                    stock: { bsonType: "int", minimum: 0 },
                    createdAt: { bsonType: "date" },
                    updatedAt: { bsonType: "date" }
                }
            }
        }
    });
    print("✔ Collection 'products' created.");
} else {
    print("ℹ Collection 'products' already exists, skipping creation.");
}

// Indexes (createIndex is safe → no error if exists)
db.products.createIndex({ name: "text", description: "text" });
db.products.createIndex({ category: 1 });
db.products.createIndex({ price: 1 });
db.products.createIndex({ createdAt: -1 });

print("✔ Product DB initialization complete.");
