package kg.kut.os.mentorhub.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SubmitApplicationRequest {

    @NotBlank(message = "Текст мотивации не может быть пустым")
    @Size(min = 10, max = 2000, message = "Текст мотивации должен быть от 10 до 2000 символов")
    private String motivationText;

    @NotBlank(message = "Описание опыта не может быть пустым")
    @Size(min = 10, max = 2000, message = "Описание опыта должно быть от 10 до 2000 символов")
    private String experienceSummary;

    @Size(max = 500, message = "URL портфолио не должен превышать 500 символов")
    private String portfolioUrl;

    public String getMotivationText() {
        return motivationText;
    }

    public String getExperienceSummary() {
        return experienceSummary;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
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
}

