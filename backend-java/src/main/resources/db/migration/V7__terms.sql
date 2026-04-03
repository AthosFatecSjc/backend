-- V7__terms.sql
ALTER TABLE energia.terms
ADD COLUMN IF NOT EXISTS is_required BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_terms_is_required
ON energia.terms (is_required);   


