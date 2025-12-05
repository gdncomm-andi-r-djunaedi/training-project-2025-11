-- Seed 5,000 members with pre-hashed password ("Password!123")
DO $$
DECLARE
    hashed_password CONSTANT TEXT := '$2a$10$Dow1FgcdMXe7ZLVdBp9o4eM0m0G2ENU1N1pAVccahJgN9zeps2gG';
BEGIN
    FOR i IN 1..5000 LOOP
        INSERT INTO members (name, email, password, role)
        VALUES (
            CONCAT('Member ', i),
            CONCAT('member', i, '@example.com'),
            hashed_password,
            'ROLE_USER'
        )
        ON CONFLICT (email) DO NOTHING;
    END LOOP;
END $$;

