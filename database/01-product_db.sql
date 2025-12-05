\connect product_db;

-- Enable UUID extension for postgres
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create product table
CREATE TABLE IF NOT EXISTS product (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    price         NUMERIC(12, 2) NOT NULL,
    category      VARCHAR(100),
    image_url     TEXT,
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);

-- Import product data from CSV automatically
-- Expected file: /docker-entrypoint-initdb.d/products_all.csv
COPY product(id, name, description, price, category, image_url, is_active,created_at, updated_at)
FROM '/docker-entrypoint-initdb.d/products_all.csv'
WITH (FORMAT csv, HEADER true);