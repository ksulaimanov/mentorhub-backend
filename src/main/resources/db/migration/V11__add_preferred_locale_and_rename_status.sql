-- Add preferred_locale column with default 'ky' (Kyrgyz)
ALTER TABLE users ADD COLUMN preferred_locale VARCHAR(10) NOT NULL DEFAULT 'ky';

-- Rename status enum value for clarity
UPDATE users SET status = 'PENDING_EMAIL_VERIFICATION' WHERE status = 'PENDING_VERIFICATION';

