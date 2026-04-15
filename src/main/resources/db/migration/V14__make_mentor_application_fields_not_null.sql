-- Make motivation_text and experience_summary NOT NULL to match DTO validation
ALTER TABLE mentor_applications ALTER COLUMN motivation_text SET NOT NULL;
ALTER TABLE mentor_applications ALTER COLUMN experience_summary SET NOT NULL;

