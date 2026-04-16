-- Add teacher address field for admin/user profile updates.
ALTER TABLE teachers
ADD COLUMN IF NOT EXISTS address VARCHAR(255);

