-- Sprint 3A: Add review audit fields and optimistic locking to mentor_applications

ALTER TABLE mentor_applications ADD COLUMN reviewed_at TIMESTAMP;
ALTER TABLE mentor_applications ADD COLUMN admin_comment TEXT;
ALTER TABLE mentor_applications ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Backfill: set reviewed_at = updated_at for already-reviewed applications
UPDATE mentor_applications SET reviewed_at = updated_at WHERE status IN ('APPROVED', 'REJECTED');

