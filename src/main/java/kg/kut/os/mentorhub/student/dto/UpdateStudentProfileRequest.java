package kg.kut.os.mentorhub.student.dto;

import jakarta.validation.constraints.Size;

public class UpdateStudentProfileRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 255)
    private String avatarKey;

    @Size(max = 2000)
    private String bio;

    @Size(max = 100)
    private String timezone;

    @Size(max = 50)
    private String phone;

    @Size(max = 100)
    private String city;

    public UpdateStudentProfileRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAvatarKey() {
        return avatarKey;
    }

    public String getBio() {
        return bio;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getPhone() {
        return phone;
    }

    public String getCity() {
        return city;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCity(String city) {
        this.city = city;
    }
}