-- Insert deterministic sequential SKU-like products
INSERT INTO products (
    product_id,
    seller_id,
    name,
    description,
    category,
    price,
    stock,
    status,
    created_at
)
SELECT
    ('00000000-0000-0000-0000-' || lpad(seq::text, 12, '0'))::uuid AS product_id,
    gen_random_uuid(),
    'Product ' || seq,
    'This is dummy product #' || seq,
    CASE 
        WHEN seq % 5 = 0 THEN 'electronics'
        WHEN seq % 5 = 1 THEN 'fashion'
        WHEN seq % 5 = 2 THEN 'beauty'
        WHEN seq % 5 = 3 THEN 'sports'
        ELSE 'home'
    END,
    (random() * 200000 + 1000)::numeric(12,2),
    (random() * 100)::int,
    'ACTIVE',
    NOW()
FROM generate_series(1, 1000) AS t(seq)
ON CONFLICT (product_id) DO NOTHING;
