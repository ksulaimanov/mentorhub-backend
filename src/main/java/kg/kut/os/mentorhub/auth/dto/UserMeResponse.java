package kg.kut.os.mentorhub.auth.dto;

import java.util.Set;

public class UserMeResponse {
    private Long userId;
    private String email;
    private Set<String> roles;
    private String role;
    private String firstName;
    private String lastName;
    private String avatarUrl;

    public UserMeResponse() {
    }

    public UserMeResponse(Long userId, String email, Set<String> roles, String role, String firstName, String lastName, String avatarUrl) {
        this.userId = userId;
        this.email = email;
        this.roles = roles;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
