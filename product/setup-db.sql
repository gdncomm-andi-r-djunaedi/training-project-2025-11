CREATE EXTENSION IF NOT EXISTS "pgcrypto";

INSERT INTO products (id, name, description, price, quantity, image_url)
SELECT
    gen_random_uuid(),
    CONCAT('Product ', g),
    CONCAT('Detailed description for product ', g),
    ROUND((random() * 990 + 10)::numeric, 2),
    50 + (random() * 450)::int,
    CONCAT('https://cdn.example.com/products/', g, '.jpg')
FROM generate_series(1, 50000) AS g
ON CONFLICT DO NOTHING;

