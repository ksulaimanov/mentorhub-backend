-- Add preferred_locale column with default 'ru'
ALTER TABLE users ADD COLUMN preferred_locale VARCHAR(10) NOT NULL DEFAULT 'ru';

-- Rename status enum value for clarity
UPDATE users SET status = 'PENDING_EMAIL_VERIFICATION' WHERE status = 'PENDING_VERIFICATION';

