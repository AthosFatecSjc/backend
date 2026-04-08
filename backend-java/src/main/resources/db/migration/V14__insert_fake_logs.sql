-- Inserir logs fakes para facilitar testes da consulta administrativa
-- Cobre diferentes eventos, atores, destinos, módulos e categorias

INSERT INTO energia.system_logs 
(actor_ref, target_ref, source_type, event, result, log_category, description, created_by_module, created_at)
VALUES
-- LOGIN attempts - variados
('user001@example.com', NULL, 'USER', 'LOGIN_SUCCESS', 'SUCCESS', 'AUDIT', 'Usuário realizou login com sucesso', 'auth-service', NOW() - INTERVAL '5 days'),
('user002@example.com', NULL, 'USER', 'LOGIN_SUCCESS', 'SUCCESS', 'AUDIT', 'Usuário realizou login com sucesso', 'auth-service', NOW() - INTERVAL '4 days' - INTERVAL '2 hours'),
('user001@example.com', NULL, 'USER', 'LOGIN_FAIL', 'FAIL', 'AUDIT', 'Falha no login - credenciais inválidas', 'auth-service', NOW() - INTERVAL '3 days'),
('user003@example.com', NULL, 'USER', 'LOGIN_FAIL', 'FAIL', 'AUDIT', 'Falha no login - conta bloqueada', 'auth-service', NOW() - INTERVAL '2 days' - INTERVAL '10 hours'),
('admin@example.com', NULL, 'USER', 'LOGIN_SUCCESS', 'SUCCESS', 'AUDIT', 'Admin realizou login com sucesso', 'auth-service', NOW() - INTERVAL '1 day'),

-- USER registration and approval
('system', 'user004@example.com', 'SYSTEM', 'USER_REGISTER', 'SUCCESS', 'AUDIT', 'Novo usuário registrado: user004@example.com', 'user-service', NOW() - INTERVAL '4 days' - INTERVAL '12 hours'),
('admin@example.com', 'user004@example.com', 'USER', 'USER_APPROVED', 'SUCCESS', 'AUDIT', 'Usuário user004@example.com aprovado por admin', 'user-service', NOW() - INTERVAL '3 days' - INTERVAL '5 hours'),
('admin@example.com', 'user005@example.com', 'USER', 'USER_REJECTED', 'SUCCESS', 'AUDIT', 'Usuário user005@example.com rejeitado', 'user-service', NOW() - INTERVAL '2 days' - INTERVAL '8 hours'),

-- USER edits
('admin@example.com', 'user002@example.com', 'USER', 'USER_EDITED', 'SUCCESS', 'AUDIT', 'Perfil de user002@example.com editado por admin', 'user-service', NOW() - INTERVAL '2 days'),
('user001@example.com', 'user001@example.com', 'USER', 'USER_EDITED', 'SUCCESS', 'AUDIT', 'Perfil atualizado pelo próprio usuário', 'user-service', NOW() - INTERVAL '1 day' - INTERVAL '3 hours'),

-- USER anonymization
('system', 'user006@example.com', 'SYSTEM', 'USER_ANONYMIZED', 'SUCCESS', 'TECHNICAL', 'Usuário user006@example.com anonimizado conforme política de dados', 'privacy-service', NOW() - INTERVAL '6 days'),
('system', 'user007@example.com', 'SYSTEM', 'USER_ANONYMIZATION_REAPPLIED', 'SUCCESS', 'TECHNICAL', 'Anonimização reaplicada para user007@example.com', 'privacy-service', NOW() - INTERVAL '5 days' - INTERVAL '6 hours'),

-- ADMIN operations
('admin@example.com', 'user002@example.com', 'USER', 'ADMIN_ROLE_GRANTED', 'SUCCESS', 'AUDIT', 'Função de administrador concedida a user002@example.com', 'admin-service', NOW() - INTERVAL '7 days'),
('admin@example.com', 'user003@example.com', 'USER', 'ADMIN_ROLE_REMOVED', 'SUCCESS', 'AUDIT', 'Função de administrador removida de user003@example.com', 'admin-service', NOW() - INTERVAL '6 days' - INTERVAL '4 hours'),

-- BACKUP and restoration
('system', 'backup_2026_04_01', 'JOB', 'BACKUP_RESTORE_RECONCILIATION', 'SUCCESS', 'TECHNICAL', 'Reconciliação de backup finalizada com sucesso', 'backup-service', NOW() - INTERVAL '8 days'),
('system', 'backup_2026_03_25', 'JOB', 'BACKUP_RESTORE_RECONCILIATION', 'FAIL', 'TECHNICAL', 'Falha na reconciliação de backup - inconsistências detectadas', 'backup-service', NOW() - INTERVAL '10 days'),

-- ANEEL extraction
('system', 'aneel_extract_001', 'JOB', 'ANEEL_EXTRACTION_START', 'SUCCESS', 'TECHNICAL', 'Início da extração de dados ANEEL', 'aneel-service', NOW() - INTERVAL '3 days' - INTERVAL '1 hour'),
('system', 'aneel_extract_001', 'JOB', 'ANEEL_EXTRACTION_SUCCESS', 'SUCCESS', 'TECHNICAL', 'Extração ANEEL finalizada: 15 registros processados', 'aneel-service', NOW() - INTERVAL '3 days'),
('system', 'aneel_extract_002', 'JOB', 'ANEEL_EXTRACTION_START', 'SUCCESS', 'TECHNICAL', 'Início da extração de dados ANEEL', 'aneel-service', NOW() - INTERVAL '2 days' - INTERVAL '2 hours'),
('system', 'aneel_extract_002', 'JOB', 'ANEEL_EXTRACTION_FAIL', 'FAIL', 'TECHNICAL', 'Falha na extração ANEEL - conexão com banco de dados perdida', 'aneel-service', NOW() - INTERVAL '2 days' - INTERVAL '1 hour'),
('system', 'aneel_extract_003', 'JOB', 'ANEEL_EXTRACTION_START', 'SUCCESS', 'TECHNICAL', 'Início da extração de dados ANEEL', 'aneel-service', NOW() - INTERVAL '1 day' - INTERVAL '3 hours'),
('system', 'aneel_extract_003', 'JOB', 'ANEEL_EXTRACTION_SUCCESS', 'SUCCESS', 'TECHNICAL', 'Extração ANEEL finalizada: 22 registros processados', 'aneel-service', NOW() - INTERVAL '1 day' - INTERVAL '2 hours'),

-- Additional login scenarios for testing
('user008@example.com', NULL, 'USER', 'LOGIN_SUCCESS', 'SUCCESS', 'AUDIT', 'Usuário realizou login com sucesso', 'auth-service', NOW() - INTERVAL '12 hours'),
('user009@example.com', NULL, 'USER', 'LOGIN_FAIL', 'FAIL', 'AUDIT', 'Falha no login - usuário não encontrado', 'auth-service', NOW() - INTERVAL '6 hours'),
('support@example.com', NULL, 'USER', 'LOGIN_SUCCESS', 'SUCCESS', 'AUDIT', 'Support realizou login com sucesso', 'auth-service', NOW() - INTERVAL '2 hours'),

-- System operations
('system', NULL, 'SYSTEM', 'LOGIN_SUCCESS', 'SUCCESS', 'TECHNICAL', 'Sistema iniciado com sucesso', 'core-service', NOW() - INTERVAL '1 day' - INTERVAL '12 hours'),

-- Module-specific operations
('user001@example.com', 'report_001', 'USER', 'USER_EDITED', 'SUCCESS', 'AUDIT', 'Relatório editado por usuário', 'report-service', NOW() - INTERVAL '5 hours'),
('admin@example.com', 'report_001', 'USER', 'USER_APPROVED', 'SUCCESS', 'AUDIT', 'Relatório aprovado por administrador', 'report-service', NOW() - INTERVAL '3 hours'),
('system', 'data_sync_job', 'JOB', 'ANEEL_EXTRACTION_START', 'SUCCESS', 'TECHNICAL', 'Job de sincronização iniciado', 'sync-service', NOW() - INTERVAL '30 minutes'),
('system', 'data_sync_job', 'JOB', 'ANEEL_EXTRACTION_SUCCESS', 'SUCCESS', 'TECHNICAL', 'Sincronização finalizada: 8 registros atualizados', 'sync-service', NOW() - INTERVAL '15 minutes');
