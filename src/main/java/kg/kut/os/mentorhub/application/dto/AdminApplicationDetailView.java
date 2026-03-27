package kg.kut.os.mentorhub.application.dto;

import kg.kut.os.mentorhub.application.entity.MentorApplicationStatus;

import java.time.LocalDateTime;

/**
 * DTO для админ-панели: детали одной заявки
 */
public class AdminApplicationDetailView {

    private Long applicationId;
    private Long applicantUserId;
    private String applicantEmail;
    private String applicantName;
    private MentorApplicationStatus status;
    private String motivationText;
    private String experienceSummary;
    private String portfolioUrl;
    private String rejectionReason;
    private Long reviewedByUserId;
    private String reviewedByEmail;
    private LocalDateTime reviewedAt;
    private String adminComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminApplicationDetailView() {
    }

    // Getters
    public Long getApplicationId() {
        return applicationId;
    }

    public Long getApplicantUserId() {
        return applicantUserId;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public String getApplicantName() {
        return applicantName;
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

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Long getReviewedByUserId() {
        return reviewedByUserId;
    }

    public String getReviewedByEmail() {
        return reviewedByEmail;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public void setApplicantUserId(Long applicantUserId) {
        this.applicantUserId = applicantUserId;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
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

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public void setReviewedByUserId(Long reviewedByUserId) {
        this.reviewedByUserId = reviewedByUserId;
    }

    public void setReviewedByEmail(String reviewedByEmail) {
        this.reviewedByEmail = reviewedByEmail;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

