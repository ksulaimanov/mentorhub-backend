CREATE INDEX idx_mentor_applications_applicant_status ON mentor_applications(applicant_user_id, status);

CREATE INDEX idx_mentor_applications_applicant_created_at ON mentor_applications(applicant_user_id, created_at);

CREATE UNIQUE INDEX idx_mentor_applications_unique_active
ON mentor_applications (applicant_user_id)
WHERE status IN ('PENDING', 'APPROVED');
