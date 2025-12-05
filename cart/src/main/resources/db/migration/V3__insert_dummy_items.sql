WITH target_cart AS (
    SELECT id AS cart_id
    FROM cart
    WHERE member_id = '11111111-1111-1111-1111-111111111111'::uuid
    LIMIT 1
),

sku_products AS (
    SELECT
        ('00000000-0000-0000-0000-' || lpad(seq::text, 12, '0'))::uuid AS product_id
    FROM generate_series(1, 1000) seq
)

INSERT INTO cart_item (id, cart_id, product_id, quantity)
SELECT
    gen_random_uuid(),
    tc.cart_id,
    sp.product_id,
    (floor(random() * 5) + 1)::int
FROM sku_products sp
CROSS JOIN target_cart tc;
