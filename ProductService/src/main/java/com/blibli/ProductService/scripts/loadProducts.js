// MongoDB Script to Load 50,000 Products
// Database: product_details
// Collection: products
// Run: mongosh product_details loadProducts.js
// Make sure products collection is empty

db = db.getSiblingDB('product_details');

const categories = [
  "Electronics", "Clothing", "Home & Kitchen", "Books", "Sports",
  "Toys", "Beauty", "Automotive", "Garden", "Food"
];

const namePrefixes = [
  "Premium", "Deluxe", "Standard", "Professional", "Classic",
  "Modern", "Vintage", "Luxury", "Basic", "Advanced"
];

const types = [
  "Widget", "Gadget", "Device", "Tool", "Accessory",
  "Item", "Product", "Equipment", "System", "Solution"
];

print("Creating 50,000 products...");

const TOTAL = 50000;
const BATCH_SIZE = 1000;
let products = [];

for (let i = 1; i <= TOTAL; i++) {

    const prefix = namePrefixes[Math.floor(Math.random() * namePrefixes.length)];
    const type = types[Math.floor(Math.random() * types.length)];

    const sku = `SKU-${i}`; // SKU will also be Mongo _id
    const productName = `${prefix} ${type} ${i}`;
    const description = `High quality ${productName.toLowerCase()} with premium features.`;
   const price = Math.floor(Math.random() * 9000) + 100;
    const category = categories[Math.floor(Math.random() * categories.length)];

    // Use sku as _id
    products.push({
        _id: sku,          // Mongo _id
        sku: sku,          // SKU field
        productName: productName,
        description: description,
        price: price,
        category: category
    });

    // Insert in batches
    if (products.length === BATCH_SIZE) {
        db.products.insertMany(products);
        print(`Inserted ${i} products...`);
        products = [];
    }
}

// Insert remaining products
if (products.length > 0) {
    db.products.insertMany(products);
}

print("✅ Products creation completed!");
print("✅ Total products in DB: " + db.products.countDocuments());
