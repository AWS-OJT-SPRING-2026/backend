-- Align document-related schema between BE Java and AI service.
-- Safe to run multiple times.

-- books: owner + upload timestamp
ALTER TABLE books
    ADD COLUMN IF NOT EXISTS create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE books
    ADD COLUMN IF NOT EXISTS user_id INT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_books_user_id'
    ) THEN
        ALTER TABLE books
            ADD CONSTRAINT fk_books_user_id
            FOREIGN KEY (user_id) REFERENCES users(userid) ON DELETE CASCADE;
    END IF;
END $$;

-- question_bank: upload timestamp
ALTER TABLE question_bank
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- classroom_materials: ensure assignment timestamp always present
DO $$
BEGIN
    IF to_regclass('public.classroom_materials') IS NOT NULL THEN
        ALTER TABLE classroom_materials
            ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

        ALTER TABLE classroom_materials
            ALTER COLUMN assigned_at SET DEFAULT CURRENT_TIMESTAMP;

        UPDATE classroom_materials
        SET assigned_at = CURRENT_TIMESTAMP
        WHERE assigned_at IS NULL;
    END IF;
END $$;

