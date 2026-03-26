package kg.kut.os.mentorhub.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectApplicationRequest {

    @NotBlank(message = "Причина отклонения не может быть пустой")
    @Size(min = 10, max = 1000, message = "Причина отклонения должна быть от 10 до 1000 символов")
    private String rejectionReason;

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}

