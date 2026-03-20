package kg.kut.os.mentorhub.auth.dto;

import java.util.Set;

public class AuthResponse {

    private Long userId;
    private String email;
    private Set<String> roles;
    private String accessToken;
    private String refreshToken;

    public AuthResponse() {
    }

    public AuthResponse(Long userId, String email, Set<String> roles, String accessToken, String refreshToken) {
        this.userId = userId;
        this.email = email;
        this.roles = roles;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}