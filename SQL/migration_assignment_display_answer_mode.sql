ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS display_answer_mode VARCHAR(40);

UPDATE assignments
SET display_answer_mode = 'IMMEDIATE'
WHERE display_answer_mode IS NULL OR TRIM(display_answer_mode) = '';

ALTER TABLE assignments
    ALTER COLUMN display_answer_mode SET NOT NULL,
    ALTER COLUMN display_answer_mode SET DEFAULT 'IMMEDIATE';

