package kg.kut.os.mentorhub.auth.dto;

import kg.kut.os.mentorhub.auth.entity.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

public class AdminUserSummaryDto {
    private Long id;
    private String email;
    private Set<String> roles;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;

    public AdminUserSummaryDto() {
    }

    public AdminUserSummaryDto(Long id, String email, Set<String> roles, UserStatus status, LocalDateTime createdAt, LocalDateTime lastActiveAt) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.status = status;
        this.createdAt = createdAt;
        this.lastActiveAt = lastActiveAt;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Set<String> getRoles() {
        return roles;
    }
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    public UserStatus getStatus() {
        return status;
    }
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }
    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
