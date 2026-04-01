-- Add duration for both TEST and ASSIGNMENT
ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS duration_minutes INTEGER;

-- Add per-student attempt timing fields
ALTER TABLE submissions
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMP(6),
    ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP(6),
    ADD COLUMN IF NOT EXISTS expired_at TIMESTAMP(6);

-- Backfill submitted_at from legacy submit_time when present
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'submissions' AND column_name = 'submit_time'
    ) THEN
        UPDATE submissions
        SET submitted_at = COALESCE(submitted_at, submit_time);
    END IF;
END $$;

