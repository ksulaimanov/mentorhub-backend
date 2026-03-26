package kg.kut.os.mentorhub.application.dto;

import kg.kut.os.mentorhub.application.entity.MentorApplicationStatus;

import java.time.LocalDateTime;

public class ApplicationStatusResponse {

    private Long applicationId;
    private MentorApplicationStatus status;
    private String motivationText;
    private String experienceSummary;
    private String portfolioUrl;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApplicationStatusResponse() {
    }

    public ApplicationStatusResponse(Long applicationId, MentorApplicationStatus status,
                                    String motivationText, String experienceSummary,
                                    String portfolioUrl, String rejectionReason,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.applicationId = applicationId;
        this.status = status;
        this.motivationText = motivationText;
        this.experienceSummary = experienceSummary;
        this.portfolioUrl = portfolioUrl;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getApplicationId() {
        return applicationId;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

