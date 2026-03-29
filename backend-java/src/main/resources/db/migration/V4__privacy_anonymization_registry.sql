ALTER TABLE energia.app_user
    ADD COLUMN anonymization_status VARCHAR(20) DEFAULT 'ACTIVE',
    ADD COLUMN anonymized_at TIMESTAMP;

UPDATE energia.app_user
SET anonymization_status = 'ACTIVE'
WHERE anonymization_status IS NULL;

ALTER TABLE energia.app_user
    ALTER COLUMN anonymization_status SET NOT NULL;

ALTER TABLE energia.app_user
    ADD CONSTRAINT chk_app_user_anonymization_status
    CHECK (anonymization_status IN ('ACTIVE', 'ANONYMIZED'));

CREATE TABLE energia.privacy_anonymization_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    anonymized_at TIMESTAMP NOT NULL,
    anonymized_by VARCHAR(100),
    reason TEXT,
    strategy_version INT NOT NULL,
    restore_action VARCHAR(20) NOT NULL,
    retention_until TIMESTAMP NOT NULL,
    last_reconciled_at TIMESTAMP,
    last_reapplied_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_privacy_anonymization_registry_entity UNIQUE (entity_type, entity_id),
    CONSTRAINT chk_privacy_entity_type CHECK (entity_type IN ('APP_USER')),
    CONSTRAINT chk_privacy_restore_action CHECK (restore_action IN ('REAPPLY', 'CONTAIN'))
);

CREATE INDEX idx_privacy_registry_active
    ON energia.privacy_anonymization_registry (active, anonymized_at);

CREATE INDEX idx_privacy_registry_retention
    ON energia.privacy_anonymization_registry (retention_until);
