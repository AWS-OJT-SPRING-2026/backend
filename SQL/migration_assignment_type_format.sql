-- Add new assignment columns for separating assignment type and question format
ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS assignment_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS format VARCHAR(50),
    ADD COLUMN IF NOT EXISTS start_time TIMESTAMP(6),
    ADD COLUMN IF NOT EXISTS end_time TIMESTAMP(6);

-- Backfill assignment_type from old type semantics when possible
UPDATE assignments
SET assignment_type = 'TEST'
WHERE assignment_type IS NULL;

-- Backfill format from legacy type values (TRAC_NGHIEM/TU_LUAN)
UPDATE assignments
SET format = CASE
    WHEN UPPER(type) IN ('TRAC_NGHIEM', 'MULTIPLE_CHOICE') THEN 'MULTIPLE_CHOICE'
    WHEN UPPER(type) IN ('TU_LUAN', 'ESSAY') THEN 'ESSAY'
    ELSE 'MULTIPLE_CHOICE'
END
WHERE format IS NULL;

-- Optional cleanup for old schema after code rollout
-- ALTER TABLE assignments DROP COLUMN type;


