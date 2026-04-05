-- Insert Status types
INSERT INTO status (id, name) VALUES
    ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'ATIVO'),
    ('f47ac10b-58cc-4372-a567-0e02b2c3d480', 'PENDENTE'),
    ('f47ac10b-58cc-4372-a567-0e02b2c3d481', 'REJEITADO')
ON CONFLICT (name) DO NOTHING;

-- Insert test users with PBKDF2 hashed password "senha123456"
-- This hash is: PBKDF2$65536$h+sUEJz0m+0HUCpmdDLkqg==$jPJ2xKnKwBYKj7hRqgUYOE4sWYHs7d5UtBQKVSAoYmc=
INSERT INTO app_user (id, name, email, password, phone) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'Active User', 'active@example.com',
     'PBKDF2$65536$h+sUEJz0m+0HUCpmdDLkqg==$jPJ2xKnKwBYKj7hRqgUYOE4sWYHs7d5UtBQKVSAoYmc=', '11999999001'),
    ('550e8400-e29b-41d4-a716-446655440002', 'Pending User', 'pending@example.com',
     'PBKDF2$65536$h+sUEJz0m+0HUCpmdDLkqg==$jPJ2xKnKwBYKj7hRqgUYOE4sWYHs7d5UtBQKVSAoYmc=', '11999999002'),
    ('550e8400-e29b-41d4-a716-446655440003', 'Rejected User', 'rejected@example.com',
     'PBKDF2$65536$h+sUEJz0m+0HUCpmdDLkqg==$jPJ2xKnKwBYKj7hRqgUYOE4sWYHs7d5UtBQKVSAoYmc=', '11999999003')
ON CONFLICT (email) DO NOTHING;

-- Assign ATIVO status to active user
INSERT INTO user_status (id, status_id, user_id, assigned_at) VALUES
    ('650e8400-e29b-41d4-a716-446655440001', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
     '550e8400-e29b-41d4-a716-446655440001', NOW());

-- Assign PENDENTE status to pending user
INSERT INTO user_status (id, status_id, user_id, assigned_at) VALUES
    ('650e8400-e29b-41d4-a716-446655440002', 'f47ac10b-58cc-4372-a567-0e02b2c3d480',
     '550e8400-e29b-41d4-a716-446655440002', NOW());

-- Assign REJEITADO status to rejected user with reason
INSERT INTO user_status (id, status_id, user_id, assigned_at, rationale_for_rejection) VALUES
    ('650e8400-e29b-41d4-a716-446655440003', 'f47ac10b-58cc-4372-a567-0e02b2c3d481',
     '550e8400-e29b-41d4-a716-446655440003', NOW(), 'Failed security verification');
