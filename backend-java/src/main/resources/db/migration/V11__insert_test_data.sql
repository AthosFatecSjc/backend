-- V6: Insert test data with BCrypt password hashes
-- Password for all test users: "senha123456"
-- BCrypt hash: $2a$10$slYQmyNdGzin7olVN3DONOYvkKbl.W0wB2.MQUB34qLwCNQnb8f1a

-- Insert Status types (if not already inserted)
INSERT INTO status (id, name) VALUES
    ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'ATIVO'),
    ('f47ac10b-58cc-4372-a567-0e02b2c3d480', 'PENDENTE'),
    ('f47ac10b-58cc-4372-a567-0e02b2c3d481', 'REJEITADO')
ON CONFLICT (name) DO NOTHING;

-- Insert test users with BCrypt password hashes
INSERT INTO app_user (id, name, email, password, phone) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'Active User', 'active@example.com',
     '$2a$10$slYQmyNdGzin7olVN3DONOYvkKbl.W0wB2.MQUB34qLwCNQnb8f1a', '11999999001'),
    ('550e8400-e29b-41d4-a716-446655440002', 'Pending User', 'pending@example.com',
     '$2a$10$slYQmyNdGzin7olVN3DONOYvkKbl.W0wB2.MQUB34qLwCNQnb8f1a', '11999999002'),
    ('550e8400-e29b-41d4-a716-446655440003', 'Rejected User', 'rejected@example.com',
     '$2a$10$slYQmyNdGzin7olVN3DONOYvkKbl.W0wB2.MQUB34qLwCNQnb8f1a', '11999999003')
ON CONFLICT (email) DO NOTHING;

-- Assign ATIVO status to active user
INSERT INTO user_status (id, status_id, user_id, assigned_at) VALUES
    ('650e8400-e29b-41d4-a716-446655440001', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
     '550e8400-e29b-41d4-a716-446655440001', NOW())
ON CONFLICT DO NOTHING;

-- Assign PENDENTE status to pending user
INSERT INTO user_status (id, status_id, user_id, assigned_at) VALUES
    ('650e8400-e29b-41d4-a716-446655440002', 'f47ac10b-58cc-4372-a567-0e02b2c3d480',
     '550e8400-e29b-41d4-a716-446655440002', NOW())
ON CONFLICT DO NOTHING;

-- Assign REJEITADO status to rejected user with reason
INSERT INTO user_status (id, status_id, user_id, assigned_at, rationale_for_rejection) VALUES
    ('650e8400-e29b-41d4-a716-446655440003', 'f47ac10b-58cc-4372-a567-0e02b2c3d481',
     '550e8400-e29b-41d4-a716-446655440003', NOW(), 'Failed security verification')
ON CONFLICT DO NOTHING;

-- Assign USER role to all test users
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u, role r
WHERE u.email IN ('active@example.com', 'pending@example.com', 'rejected@example.com')
  AND r.name = 'user'
ON CONFLICT DO NOTHING;
