-- --------------------------------------------------
-- Load 5000 Users into auth_service.users
-- Run:
-- psql -U postgres -d auth_service -f load-users.sql
-- --------------------------------------------------

-- Enable pgcrypto if not already enabled
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    i INTEGER;
    v_username TEXT;
    v_email TEXT;
    v_password TEXT := crypt('Admin@123456', gen_salt('bf'));
BEGIN
    FOR i IN 1..5000 LOOP

        v_username := 'user' || i;
        v_email := 'user' || i || '@example.com';

        INSERT INTO users (username, password, email)
        VALUES (v_username, v_password, v_email);

        IF i % 500 = 0 THEN
            RAISE NOTICE 'Inserted % users...', i;
        END IF;

    END LOOP;

    RAISE NOTICE '✅ 5000 users inserted successfully';
END $$;
