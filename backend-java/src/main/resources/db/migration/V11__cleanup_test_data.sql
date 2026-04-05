-- Delete test users and statuses created by V5
DELETE FROM user_status WHERE user_id IN (
    SELECT id FROM app_user
    WHERE email IN ('active@example.com', 'pending@example.com', 'rejected@example.com')
);

DELETE FROM app_user
WHERE email IN ('active@example.com', 'pending@example.com', 'rejected@example.com');

DELETE FROM status
WHERE name IN ('ATIVO', 'PENDENTE', 'REJEITADO');
