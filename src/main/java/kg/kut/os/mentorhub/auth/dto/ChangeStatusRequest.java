package kg.kut.os.mentorhub.auth.dto;

import kg.kut.os.mentorhub.auth.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public class ChangeStatusRequest {
    @NotNull(message = "Status is required")
    private UserStatus status;

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
