CREATE EXTENSION IF NOT EXISTS "pgcrypto";

INSERT INTO products (id, name, description, price, quantity, image_url)
SELECT
    gen_random_uuid(),
    CONCAT('Product ', g),
    CONCAT('Detailed description for product ', g),
    ((random() * 990)::int + 10),
    2147483647,
    CONCAT('https://cdn.example.com/products/', g, '.jpg')
FROM generate_series(1, 50000) AS g
ON CONFLICT DO NOTHING;

