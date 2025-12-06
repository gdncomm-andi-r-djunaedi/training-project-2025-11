-- Insert one test cart with known member id for lookup.
-- You can change the MEMBER_ID value to match your test member.

INSERT INTO cart (id, member_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    '11111111-1111-1111-1111-111111111111'::uuid, -- test member id (fixed)
    now(),
    now()
);
