ALTER TABLE energia.app_user
RENAME COLUMN anonymization_status TO deletion_status;

ALTER TABLE energia.app_user
RENAME COLUMN anonymized_at TO deleted_at;

ALTER TABLE energia.app_user
DROP CONSTRAINT IF EXISTS chk_app_user_anonymization_status;

ALTER TABLE energia.app_user
ADD CONSTRAINT chk_app_user_deletion_status
CHECK (deletion_status IN ('ACTIVE', 'DELETED'));

-- Renomeia a tabela
ALTER TABLE energia.privacy_anonymization_registry
RENAME TO privacy_deletion_registry;

-- Renomeia colunas
ALTER TABLE energia.privacy_deletion_registry
RENAME COLUMN anonymized_at TO deleted_at;

ALTER TABLE energia.privacy_deletion_registry
RENAME COLUMN anonymized_by TO deleted_by;

-- Renomeia constraint UNIQUE
ALTER TABLE energia.privacy_deletion_registry
RENAME CONSTRAINT uq_privacy_anonymization_registry_entity
TO uq_privacy_deletion_registry_entity;

-- Remove constraints antigas
ALTER TABLE energia.privacy_deletion_registry
DROP CONSTRAINT IF EXISTS chk_privacy_entity_type;

ALTER TABLE energia.privacy_deletion_registry
DROP CONSTRAINT IF EXISTS chk_privacy_restore_action;

-- Cria novas constraints
ALTER TABLE energia.privacy_deletion_registry
ADD CONSTRAINT chk_privacy_deletion_entity_type
CHECK (entity_type IN ('APP_USER'));

ALTER TABLE energia.privacy_deletion_registry
ADD CONSTRAINT chk_privacy_deletion_restore_action
CHECK (restore_action IN ('REAPPLY', 'CONTAIN'));

-- Remove índices antigos
DROP INDEX IF EXISTS energia.idx_privacy_registry_active;

DROP INDEX IF EXISTS energia.idx_privacy_registry_retention;

-- Cria índices corrigidos
CREATE INDEX idx_privacy_deletion_registry_active
    ON energia.privacy_deletion_registry (active, deleted_at);

CREATE INDEX idx_privacy_deletion_registry_retention
    ON energia.privacy_deletion_registry (retention_until);

ALTER TABLE energia.privacy_deletion_registry
ALTER COLUMN strategy_version DROP NOT NULL;

ALTER TABLE energia.privacy_deletion_registry
ALTER COLUMN restore_action DROP NOT NULL;

ALTER TABLE energia.privacy_deletion_registry
ALTER COLUMN retention_until DROP NOT NULL;

ALTER TABLE energia.privacy_deletion_registry
ALTER COLUMN active DROP NOT NULL;