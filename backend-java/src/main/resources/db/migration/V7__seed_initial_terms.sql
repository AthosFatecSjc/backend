INSERT INTO energia.term_type (id, name)
SELECT gen_random_uuid(), 'TERMS_OF_USE'
WHERE NOT EXISTS (
    SELECT 1
    FROM energia.term_type
    WHERE name = 'TERMS_OF_USE'
);

INSERT INTO energia.term_type (id, name)
SELECT gen_random_uuid(), 'PRIVACY_POLICY'
WHERE NOT EXISTS (
    SELECT 1
    FROM energia.term_type
    WHERE name = 'PRIVACY_POLICY'
);

INSERT INTO energia.term_type (id, name)
SELECT gen_random_uuid(), 'MARKETING_COMMUNICATION'
WHERE NOT EXISTS (
    SELECT 1
    FROM energia.term_type
    WHERE name = 'MARKETING_COMMUNICATION'
);

UPDATE energia.terms t
SET is_required = TRUE
FROM energia.term_type tt
WHERE t.term_type_id = tt.id
  AND t.version = 1
  AND tt.name IN ('TERMS_OF_USE', 'PRIVACY_POLICY');

UPDATE energia.terms t
SET is_required = FALSE
FROM energia.term_type tt
WHERE t.term_type_id = tt.id
  AND t.version = 1
  AND tt.name = 'MARKETING_COMMUNICATION';

INSERT INTO energia.terms (
    id,
    term_type_id,
    version,
    created_at,
    effectivity_start_at,
    effectivity_end_at,
    content,
    is_required
)
SELECT
    gen_random_uuid(),
    tt.id,
    1,
    TIMESTAMP '2026-03-01 00:00:00',
    TIMESTAMP '2026-03-01 00:00:00',
    NULL,
    'Termo de uso inicial para desenvolvimento. Texto fake para desbloquear a integracao do frontend enquanto o conteudo juridico oficial nao e publicado.',
    TRUE
FROM energia.term_type tt
WHERE tt.name = 'TERMS_OF_USE'
  AND NOT EXISTS (
      SELECT 1
      FROM energia.terms t
      WHERE t.term_type_id = tt.id
        AND t.version = 1
  );

INSERT INTO energia.terms (
    id,
    term_type_id,
    version,
    created_at,
    effectivity_start_at,
    effectivity_end_at,
    content,
    is_required
)
SELECT
    gen_random_uuid(),
    tt.id,
    1,
    TIMESTAMP '2026-03-01 00:00:00',
    TIMESTAMP '2026-03-01 00:00:00',
    NULL,
    'Politica de privacidade inicial para desenvolvimento. Texto fake para desbloquear a integracao do frontend enquanto o conteudo juridico oficial nao e publicado.',
    TRUE
FROM energia.term_type tt
WHERE tt.name = 'PRIVACY_POLICY'
  AND NOT EXISTS (
      SELECT 1
      FROM energia.terms t
      WHERE t.term_type_id = tt.id
        AND t.version = 1
  );

INSERT INTO energia.terms (
    id,
    term_type_id,
    version,
    created_at,
    effectivity_start_at,
    effectivity_end_at,
    content,
    is_required
)
SELECT
    gen_random_uuid(),
    tt.id,
    1,
    TIMESTAMP '2026-03-01 00:00:00',
    TIMESTAMP '2026-03-01 00:00:00',
    NULL,
    'Termo de comunicacao de marketing inicial para desenvolvimento. Texto fake e opcional para testes do frontend.',
    FALSE
FROM energia.term_type tt
WHERE tt.name = 'MARKETING_COMMUNICATION'
  AND NOT EXISTS (
      SELECT 1
      FROM energia.terms t
      WHERE t.term_type_id = tt.id
        AND t.version = 1
  );
