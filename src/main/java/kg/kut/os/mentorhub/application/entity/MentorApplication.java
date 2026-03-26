package kg.kut.os.mentorhub.application.entity;

import jakarta.persistence.*;
import kg.kut.os.mentorhub.auth.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_applications")
public class MentorApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_user_id", nullable = false)
    private User applicantUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentorApplicationStatus status;

    @Column(columnDefinition = "text")
    private String motivationText;

    @Column(columnDefinition = "text")
    private String experienceSummary;

    @Column(length = 500)
    private String portfolioUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedByUser;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public MentorApplication() {
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getApplicantUser() {
        return applicantUser;
    }

    public MentorApplicationStatus getStatus() {
        return status;
    }

    public String getMotivationText() {
        return motivationText;
    }

    public String getExperienceSummary() {
        return experienceSummary;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public User getReviewedByUser() {
        return reviewedByUser;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setApplicantUser(User applicantUser) {
        this.applicantUser = applicantUser;
    }

    public void setStatus(MentorApplicationStatus status) {
        this.status = status;
    }

    public void setMotivationText(String motivationText) {
        this.motivationText = motivationText;
    }

    public void setExperienceSummary(String experienceSummary) {
        this.experienceSummary = experienceSummary;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }

    public void setReviewedByUser(User reviewedByUser) {
        this.reviewedByUser = reviewedByUser;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}

