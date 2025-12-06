-- This script populates the 'product' table with 50,000 dummy records.
-- It assumes the 'id' column is a UUID and will be generated automatically by the database (e.g., using uuid_generate_v4() or gen_random_uuid() as a default value).
-- If your PostgreSQL version is older than 13, you might need to run: CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- To clear existing data in the table before running, you can use:
-- TRUNCATE TABLE product RESTART IDENTITY;

INSERT INTO product (id, product_id, product_name, product_price, product_detail, product_notes, product_image)
SELECT
    gen_random_uuid(), -- Generate a random UUID for the primary key
    'TESTS-' || lpad(s.i::text, 6, '0'),
    'Product Name ' || s.i,
    (random() * 999 + 1)::numeric(10, 2), -- Generates a price between 1.00 and 1000.00
    'This is a detailed description for product ' || s.i || '. It has many features and benefits that will surely satisfy your needs.',
    'Special notes for product ' || s.i,
    'https://placehold.co/600x400' || s.i
FROM
    generate_series(1, 50000) AS s(i);
