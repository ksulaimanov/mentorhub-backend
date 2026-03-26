CREATE TABLE mentor_applications (
    id                   BIGSERIAL PRIMARY KEY,
    applicant_user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    motivation_text      TEXT,
    experience_summary   TEXT,
    portfolio_url        VARCHAR(500),
    reviewed_by_user_id  BIGINT REFERENCES users(id) ON DELETE SET NULL,
    rejection_reason     TEXT,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mentor_applications_applicant ON mentor_applications(applicant_user_id);
CREATE INDEX idx_mentor_applications_status ON mentor_applications(status);
CREATE INDEX idx_mentor_applications_reviewed_by ON mentor_applications(reviewed_by_user_id);

