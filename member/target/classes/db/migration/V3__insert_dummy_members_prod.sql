-- Clear members table
DELETE FROM members;

-- STATIC DUMMY USERS (2 fixed)
INSERT INTO members (id, full_name, email, password_hash, phone_number, avatar_url, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Test User', 'testuser@example.com',
        '$2a$10$eB7oS1hLkKxD9n8gK3wQzu7F3o6pO5Vb7d1s43g0KQKb1x0u0n9y6',
        '081234567800', NULL, NOW() - INTERVAL '10 days', NOW()),

    (gen_random_uuid(), 'Demo Member', 'demo@example.com',
        '$2a$10$eB7oS1hLkKxD9n8gK3wQzu7F3o6pO5Vb7d1s43g0KQKb1x0u0n9y6',
        '081234567801', NULL, NOW() - INTERVAL '5 days', NOW());

-- DYNAMIC GENERATOR (up to 5000 users)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 5000
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
    NOW() - ((n % 30) || ' days')::interval
FROM names;
