ALTER TABLE mentor_profiles ADD COLUMN reviews_count INT NOT NULL DEFAULT 0;

-- Optional: update existing review counts if any reviews already exist.
UPDATE mentor_profiles mp
SET reviews_count = (
    SELECT COUNT(*) FROM reviews r WHERE r.mentor_id = mp.id
);

