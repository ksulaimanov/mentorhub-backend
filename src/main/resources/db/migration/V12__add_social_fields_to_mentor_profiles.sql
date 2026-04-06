-- Add social/contact fields to mentor_profiles
ALTER TABLE mentor_profiles ADD COLUMN instagram_url VARCHAR(500);
ALTER TABLE mentor_profiles ADD COLUMN telegram_username VARCHAR(100);
ALTER TABLE mentor_profiles ADD COLUMN public_email VARCHAR(255);

