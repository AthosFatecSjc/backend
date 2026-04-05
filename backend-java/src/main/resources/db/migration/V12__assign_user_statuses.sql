-- Get status IDs
-- First, ensure status types exist
INSERT INTO status (id, name) VALUES
    ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'ATIVO'),
    ('f47ac10b-58cc-4372-a567-0e02b2c3d480', 'PENDENTE'),
    ('f47ac10b-58cc-4372-a567-0e02b2c3d481', 'REJEITADO')
ON CONFLICT (name) DO NOTHING;

-- Update active user status to ATIVO
-- First delete current status
DELETE FROM user_status WHERE user_id = (
    SELECT id FROM app_user WHERE email = 'active@example.com'
);

-- Then insert ATIVO status
INSERT INTO user_status (id, status_id, user_id, assigned_at)
SELECT
    gen_random_uuid(),
    (SELECT id FROM status WHERE name = 'ATIVO'),
    id,
    NOW()
FROM app_user
WHERE email = 'active@example.com'
ON CONFLICT DO NOTHING;

-- Keep pending user with PENDENTE status (already default)
-- Keep rejected user with PENDENTE status, will update to REJEITADO
DELETE FROM user_status WHERE user_id = (
    SELECT id FROM app_user WHERE email = 'rejected@example.com'
);

-- Insert REJEITADO status for rejected user
INSERT INTO user_status (id, status_id, user_id, assigned_at, rationale_for_rejection)
SELECT
    gen_random_uuid(),
    (SELECT id FROM status WHERE name = 'REJEITADO'),
    id,
    NOW(),
    'Failed security verification'
FROM app_user
WHERE email = 'rejected@example.com'
ON CONFLICT DO NOTHING;
