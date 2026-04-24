DELETE FROM energia.terms;
DELETE FROM energia.user_terms;
DELETE FROM energia.term_type;

INSERT INTO energia.term_type (id, name, is_required)
SELECT gen_random_uuid(), 'TERMS_OF_USE', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM energia.term_type WHERE name = 'TERMS_OF_USE'
);

INSERT INTO energia.term_type (id, name, is_required)
SELECT gen_random_uuid(), 'PRIVACY_POLICY', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM energia.term_type WHERE name = 'PRIVACY_POLICY'
);

INSERT INTO energia.term_type (id, name, is_required)
SELECT gen_random_uuid(), 'MARKETING_COMMUNICATION', FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM energia.term_type WHERE name = 'MARKETING_COMMUNICATION'
);
