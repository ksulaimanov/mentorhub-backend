package kg.kut.os.mentorhub.application.dto;

import jakarta.validation.constraints.Size;

public class ApproveApplicationRequest {

    @Size(max = 1000, message = "Комментарий не должен превышать 1000 символов")
    private String adminComment;

    public ApproveApplicationRequest() {
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }
}

