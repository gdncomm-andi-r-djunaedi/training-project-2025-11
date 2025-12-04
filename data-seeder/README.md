# Data Seeder Module

Standalone module for seeding test data into the marketplace databases.

## Overview

This module generates realistic test data:
- **5,000 Members** → PostgreSQL (`marketplace_member` database)
- **50,000 Products** → MongoDB (`marketplace_product` database)

## Prerequisites

Before running the seeder, ensure:
1. PostgreSQL is running on `localhost:5432`
2. MongoDB is running on `localhost:27017`
3. Database `marketplace_member` exists in PostgreSQL
4. Member service has run at least once (to create the schema via JPA auto-DDL)

## Usage

### From Project Root

```bash
# Build all modules first
mvn clean install -DskipTests

# Run the seeder
cd data-seeder
mvn spring-boot:run
```

### With Custom Database Configuration

```bash
cd data-seeder
mvn spring-boot:run \
  -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://your-host:5432/your-db --spring.data.mongodb.uri=mongodb://your-host:27017/your-db"
```

## Features

- **Idempotent**: Safe to run multiple times. Skips seeding if data already exists.
- **Batch Processing**: Inserts data in batches (500 for PostgreSQL, 1000 for MongoDB) for performance.
- **Realistic Data**: Uses DataFaker library for realistic names, emails, addresses, product descriptions.
- **Progress Logging**: Shows batch progress during seeding.

## Data Generated

### Members
- Unique email addresses
- Hashed passwords (default: `Password123!`)
- Full names, addresses, phone numbers
- Role: `ROLE_USER`

### Products
- 10 categories: Electronics, Clothing, Home & Garden, Sports, Books, Toys, Beauty, Automotive, Food, Health
- Realistic product names and descriptions
- Category-appropriate pricing
- Random stock levels (10-1000)

## Configuration

Edit `src/main/resources/application.properties` to customize:
- Database connections
- Logging levels

## Performance

Typical execution time:
- Members (5,000): ~10-20 seconds
- Products (50,000): ~30-60 seconds
- Total: ~1-2 minutes

## Notes

- This module does NOT start a web server (`spring.main.web-application-type=none`)
- The application exits automatically after seeding completes

