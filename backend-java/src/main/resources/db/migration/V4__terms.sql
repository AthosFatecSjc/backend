-- Terms
ALTER TABLE energia.terms
ADD COLUMN is_required BOOLEAN NOT NULL DEFAULT FALSE;

-- Index (opcional, só se você for filtrar muito por isso)
CREATE INDEX idx_terms_is_required
ON energia.terms (is_required);