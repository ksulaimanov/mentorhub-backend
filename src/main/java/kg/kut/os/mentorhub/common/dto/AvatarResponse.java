package kg.kut.os.mentorhub.common.dto;

public class AvatarResponse {

    private String avatarKey;
    private String avatarUrl;

    public AvatarResponse() {
    }

    public AvatarResponse(String avatarKey, String avatarUrl) {
        this.avatarKey = avatarKey;
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarKey() {
        return avatarKey;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}