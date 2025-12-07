# Waroenk Catalog Seed Data

This directory contains seed data files that are automatically loaded when the application starts using Mongock migrations.

## Automatic Seeding (Mongock)

When the application starts, the following data is automatically seeded:

| Entity | Count | Migration |
|--------|-------|-----------|
| Brands | 150 | `V002_SeedBrandsAndCategories` |
| Categories | 100 | `V002_SeedBrandsAndCategories` |
| Merchants | 100 | `V003_SeedMerchants` |

### How it Works

1. **Checksum Validation**: Each seed file has a SHA-256 checksum stored in the `seed_checksums` collection
2. **Skip if Unchanged**: If the file hasn't changed, seeding is skipped
3. **Re-seed on Change**: If you modify a JSON file, it will be re-seeded on next app start

### Seed Files

- `brands.json` - Brand data with real company names and Unsplash images
- `categories.json` - Category hierarchy with parent-child relationships
- `merchants.json` - Merchant data with contact info and locations

## Product Seeding (API)

Products and variants are generated via API to ensure proper sync with TypeSense.

### Generate Products for a Merchant

```bash
# Generate 100 products for a specific merchant
curl -X POST "http://localhost:8080/api/v1/seed/products/MCH00001?count=100"

# Generate 1000 products
curl -X POST "http://localhost:8080/api/v1/seed/products/MCH00001?count=1000"
```

### Generate Products for All Merchants

```bash
# Generate 100 products per merchant (merchants 1-100)
curl -X POST "http://localhost:8080/api/v1/seed/products/all?productsPerMerchant=100"

# Generate in batches (useful for large datasets)
curl -X POST "http://localhost:8080/api/v1/seed/products/all?productsPerMerchant=1000&batchStart=1&batchEnd=10"
curl -X POST "http://localhost:8080/api/v1/seed/products/all?productsPerMerchant=1000&batchStart=11&batchEnd=20"
# ... continue until batch 100
```

### Check Seed Status

```bash
curl "http://localhost:8080/api/v1/seed/status"
```

Response:
```json
{
  "brands": 150,
  "categories": 100,
  "merchants": 100,
  "products": 10000,
  "variants": 20000
}
```

### Delete All Products (Reset)

```bash
curl -X DELETE "http://localhost:8080/api/v1/seed/products"
```

## Product Data Features

Generated products include:

### Realistic Names
- Combined brand name + product type (e.g., "Apple Pro Max Smartphone")
- Category-specific product templates

### Rich Descriptions
- Short descriptions for quick overview
- Long HTML descriptions with features and specifications

### Real Images
- Unsplash images based on category
- Thumbnail and multiple media images per variant

### Realistic Variants
- 1-3 variants per product
- Color variations with realistic color names
- Size variations for clothing categories
- Price variations between variants

## Modifying Seed Data

### To add more brands:
1. Edit `brands.json`
2. Restart the application
3. New brands will be added (existing ones replaced)

### To add more categories:
1. Edit `categories.json`
2. Follow parent-child relationship structure
3. Restart the application

### To add more merchants:
1. Edit `merchants.json`
2. Use unique `code` for each merchant (e.g., MCH00101)
3. Restart the application

## JSON Schema Reference

### Brand
```json
{
  "id": "brand-001",
  "name": "Apple",
  "slug": "apple",
  "iconUrl": "https://images.unsplash.com/..."
}
```

### Category
```json
{
  "id": "cat-electronics",
  "name": "Electronics",
  "slug": "electronics",
  "parentId": null,
  "iconUrl": "https://images.unsplash.com/..."
}
```

### Merchant
```json
{
  "id": "mch-001",
  "name": "TechZone Electronics",
  "code": "MCH00001",
  "iconUrl": "https://images.unsplash.com/...",
  "location": "Jakarta Selatan",
  "contact": {
    "phone": "+6281234567001",
    "email": "support@techzone.co.id"
  },
  "rating": 4.8
}
```

## Quick Start

1. Start the application - brands, categories, and merchants are auto-seeded
2. Generate products:
   ```bash
   # For testing (small dataset)
   curl -X POST "http://localhost:8080/api/v1/seed/products/all?productsPerMerchant=10"
   
   # For full dataset (100k products)
   curl -X POST "http://localhost:8080/api/v1/seed/products/all?productsPerMerchant=1000"
   ```
3. Verify with status endpoint:
   ```bash
   curl "http://localhost:8080/api/v1/seed/status"
   ```








