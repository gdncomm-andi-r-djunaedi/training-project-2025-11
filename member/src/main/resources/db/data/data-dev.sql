-- Clean members table
DELETE FROM members;

-- How many users to generate
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50   -- <== CHANGE HERE (50 → 1000, etc)
),

-- Array of first names
first_names AS (
    SELECT ARRAY[
        'Ahmad','Budi','Citra','Dewi','Eka','Fitri','Gita','Hadi','Indah','Joko',
        'Kartika','Lestari','Made','Nia','Omar','Putra','Ratih','Sari','Toni','Umar',
        'Vina','Wati','Yanto','Zahra','Andi','Bella','Dimas','Rani','Fajar','Heru',
        'Siti','Rudi','Maya','Agus','Lina','Reza','Nina','Bayu','Ayu','Doni',
        'Rina','Ari','Dian','Yudi','Sri','Tari','Eko','Mega','Bambang'
    ] AS arr
),

-- Array of last names
last_names AS (
    SELECT ARRAY[
        'Pratama','Santoso','Wijaya','Kusuma','Saputra','Wibowo','Permata','Kusumah','Hartono','Gunawan',
        'Setiawan','Nugroho','Rahmawati','Hidayat','Putra','Sari','Lestari','Firmansyah','Suryanto','Budiman',
        'Kurniawan','Hakim','Sutanto','Ramadhan','Sulaiman','Haryanto','Oktaviani','Pranata','Susanto','Utami',
        'Mahendra','Surya','Purnama','Ardiansyah','Novita','Rizki','Mulyadi','Safitri','Hermawan','Saputri',
        'Irawan','Ananda','Suharto','Rahayu','Widodo','Halim','Tarigan','Yuniar','Adiputra','Cahyani'
    ] AS arr
),

names AS (
    SELECT
        n,
        (SELECT arr[MOD(ABS(HASH('fn' || n)), CARDINALITY(arr)) + 1] FROM first_names) AS fname,
        (SELECT arr[MOD(ABS(HASH('ln' || n)), CARDINALITY(arr)) + 1] FROM last_names) AS lname
    FROM seq
)

INSERT INTO members (id, full_name, email, password_hash, phone_number, avatar_url, created_at, updated_at)
SELECT 
    RANDOM_UUID(),
    fname || ' ' || lname AS full_name,
    LOWER(fname || '.' || lname || n || '@mail.com') AS email,
    'pw-' || RANDOM_UUID() AS password_hash,
    '08' || MOD(ABS(HASH(n)), 90000000) + 10000000,
    CASE MOD(n, 3)
        WHEN 0 THEN 'https://i.pravatar.cc/150?img=' || MOD(n, 70)
        WHEN 1 THEN 'https://api.dicebear.com/7.x/avataaars/svg?seed=' || n
        ELSE NULL
    END,
    CURRENT_TIMESTAMP - (MOD(n, 730)) DAY,
    CURRENT_TIMESTAMP - (MOD(n, 30)) DAY
FROM names;
