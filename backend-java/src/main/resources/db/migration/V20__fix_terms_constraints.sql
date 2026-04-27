-- Garantir valores permitidos
ALTER TABLE term_type
ADD CONSTRAINT chk_term_type_name
CHECK (name IN (
    'TERMS_OF_USE',
    'PRIVACY_POLICY',
    'MARKETING_COMMUNICATION'
));

ALTER TABLE term_type
ADD CONSTRAINT uq_term_type_name UNIQUE (name);

ALTER TABLE terms
ADD COLUMN is_active BOOLEAN DEFAULT false NOT NULL,
DROP COLUMN created_at,
DROP COLUMN effectivity_end_at;

ALTER TABLE user_terms
DROP COLUMN accepted_from_ip,
DROP COLUMN event_type;

ALTER TABLE user_terms
ADD COLUMN action VARCHAR(20);

ALTER TABLE user_terms
ADD COLUMN action_at TIMESTAMP;

-- 2. Migrar dados existentes

-- ACCEPTED
UPDATE user_terms
SET action = 'ACCEPTED',
    action_at = accepted_at
WHERE accepted_at IS NOT NULL;

-- REVOKED (prioridade sobre accepted)
UPDATE user_terms
SET action = 'REVOKED',
    action_at = revoked_at
WHERE revoked_at IS NOT NULL;

-- ACKNOWLEDGED (fallback)
UPDATE user_terms
SET action = 'ACKNOWLEDGED',
    action_at = TIMESTAMP '2000-01-05' -- data arbitrária para indicar reconhecimento sem timestamp específico
WHERE action IS NULL;

-- 3. Garantir NOT NULL
ALTER TABLE user_terms
ALTER COLUMN action SET NOT NULL;

ALTER TABLE user_terms
ALTER COLUMN action_at SET NOT NULL;

-- 4. Remover colunas antigas
ALTER TABLE user_terms DROP COLUMN accepted_at;
ALTER TABLE user_terms DROP COLUMN revoked_at;

-- 5. Constraint de valores válidos
ALTER TABLE user_terms
ADD CONSTRAINT chk_user_terms_action
CHECK (action IN ('ACKNOWLEDGED', 'ACCEPTED', 'REVOKED'));

ALTER TABLE term_type
ADD COLUMN is_required BOOLEAN;

UPDATE term_type
SET is_required = CASE
    WHEN name = 'MARKETING_COMMUNICATION' THEN FALSE
    WHEN name IN ('TERMS_OF_USE', 'PRIVACY_POLICY') THEN TRUE
    ELSE is_required
END;

ALTER TABLE term_type
ALTER COLUMN is_required SET NOT NULL;

ALTER TABLE terms
DROP COLUMN is_required;

ALTER TABLE terms
ADD COLUMN effectivity_end_at TIMESTAMP;

ALTER TABLE terms ADD COLUMN clause INT DEFAULT 0;
UPDATE terms SET clause = 0 WHERE clause IS NULL;
ALTER TABLE terms ALTER COLUMN clause SET NOT NULL;

ALTER TABLE terms
DROP COLUMN version,
DROP COLUMN is_active;