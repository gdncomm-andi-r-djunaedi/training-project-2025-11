-- Enable pgcrypto extension (required for gen_random_uuid())
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create products table with proper UUID columns
CREATE TABLE IF NOT EXISTS products (
    product_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id       UUID NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    category        VARCHAR(100),
    price           NUMERIC(12,2) NOT NULL,
    stock           INTEGER NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ DEFAULT NOW()
);
