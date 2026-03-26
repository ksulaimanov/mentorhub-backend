package kg.kut.os.mentorhub.application.dto;

import kg.kut.os.mentorhub.application.entity.MentorApplicationStatus;

import java.time.LocalDateTime;

/**
 * DTO для админ-панели: список всех заявок
 */
public class AdminApplicationView {

    private Long applicationId;
    private Long applicantUserId;
    private String applicantEmail;
    private String applicantName;
    private MentorApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminApplicationView() {
    }

    public AdminApplicationView(Long applicationId, Long applicantUserId, String applicantEmail,
                              String applicantName, MentorApplicationStatus status,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.applicationId = applicationId;
        this.applicantUserId = applicantUserId;
        this.applicantEmail = applicantEmail;
        this.applicantName = applicantName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

