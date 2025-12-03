-- ======================================================
--  STATIC SEED DATA (2 USERS)
-- ======================================================

-- Clear table (safe for prod dummy seeding)
DELETE FROM members;

-- Insert STATIC test member #1
INSERT INTO members (
    id, full_name, email, password_hash, phone_number, avatar_url, created_at, updated_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Test User',
    'test.user@example.com',
    '$2a$10$KbQi2CqkfNNpJBWlAbIYW.sdN6iYG3PaY5E9OQj1YxDCEV4VpWw2e', -- Password123!
    '081234567890',
    'https://i.pravatar.cc/150?img=11',
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '1 day'
);

-- Insert STATIC test member #2
INSERT INTO members (
    id, full_name, email, password_hash, phone_number, avatar_url, created_at, updated_at
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    'Admin User',
    'admin@example.com',
    '$2a$10$KbQi2CqkfNNpJBWlAbIYW.sdN6iYG3PaY5E9OQj1YxDCEV4VpWw2e', -- Password123!
    '089876543210',
    'https://i.pravatar.cc/150?img=22',
    NOW() - INTERVAL '20 days',
    NOW() - INTERVAL '3 days'
);

-- ======================================================
--  DYNAMIC BULK DATA (up to 5000 members)
-- ======================================================

WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 5000   -- change to 50/5000 as needed
),

first_names AS (
    SELECT ARRAY[
        'Ahmad','Budi','Citra','Dewi','Eka','Fitri','Gita','Hadi','Indah','Joko',
        'Kartika','Lestari','Made','Nia','Omar','Putra','Ratih','Sari','Toni','Umar',
        'Vina','Wati','Yanto','Zahra','Andi','Bella','Dimas','Rani','Fajar','Heru',
        'Siti','Rudi','Maya','Agus','Lina','Reza','Nina','Bayu','Ayu','Doni',
        'Rina','Ari','Dian','Yudi','Sri','Tari','Eko','Mega','Bambang'
    ]::text[] AS arr
),

last_names AS (
    SELECT ARRAY[
        'Pratama','Santoso','Wijaya','Kusuma','Saputra','Wibowo','Permata','Kusumah','Hartono','Gunawan',
        'Setiawan','Nugroho','Rahmawati','Hidayat','Putra','Sari','Lestari','Firmansyah','Suryanto','Budiman',
        'Kurniawan','Hakim','Sutanto','Ramadhan','Sulaiman','Haryanto','Oktaviani','Pranata','Susanto','Utami',
        'Mahendra','Surya','Purnama','Ardiansyah','Novita','Rizki','Mulyadi','Safitri','Hermawan','Saputri',
        'Irawan','Ananda','Suharto','Rahayu','Widodo','Halim','Tarigan','Yuniar','Adiputra','Cahyani'
    ]::text[] AS arr
),

names AS (
    SELECT 
        n,
        (SELECT arr[(ABS(hashtext('fn' || n)) % array_length(arr, 1)) + 1] FROM first_names) AS fname,
        (SELECT arr[(ABS(hashtext('ln' || n)) % array_length(arr, 1)) + 1] FROM last_names) AS lname
    FROM seq
)

INSERT INTO members (id, full_name, email, password_hash, phone_number, avatar_url, created_at, updated_at)
SELECT
    gen_random_uuid(),
    fname || ' ' || lname AS full_name,
    LOWER(fname || '.' || lname || n || '@example.com') AS email,
    '$2a$10$' || substring(md5(random()::text) FROM 1 FOR 53) AS password_hash,
    '08' || LPAD((ABS((n * 7919) % 90000000)::text), 8, '0') AS phone_number,
    CASE (n % 3)
        WHEN 0 THEN 'https://i.pravatar.cc/150?img=' || ((n % 70) + 1)
        WHEN 1 THEN 'https://api.dicebear.com/7.x/avataaars/svg?seed=' || n
        ELSE NULL
    END AS avatar_url,
    NOW() - ((n % 730) || ' days')::interval,
    NOW() - ((n % 30) || ' days')::interval;
