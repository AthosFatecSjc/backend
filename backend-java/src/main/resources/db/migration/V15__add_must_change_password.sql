-- V15: Adiciona flag para exigir troca de senha no primeiro acesso
ALTER TABLE energia.app_user
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Usuários de seed/test já possuem senha conhecida, marcar para troca
UPDATE energia.app_user
SET must_change_password = TRUE
WHERE email IN ('admin@energia.com', 'user@energia.com', 'pending@energia.com');
