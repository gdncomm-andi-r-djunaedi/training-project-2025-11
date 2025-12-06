-- Enable pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- CART table
CREATE TABLE IF NOT EXISTS cart (
    id UUID PRIMARY KEY,
    member_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- CART ITEM table
CREATE TABLE IF NOT EXISTS cart_item (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES cart (id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_cart_item_cart_id ON cart_item (cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_product_id ON cart_item (product_id);
