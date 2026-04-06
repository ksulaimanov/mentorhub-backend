package kg.kut.os.mentorhub.mentor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateMentorProfileRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 255)
    private String headline;

    @Size(max = 3000)
    private String bio;

    @Size(max = 255)
    private String specialization;

    @Min(0)
    private Integer yearsExperience;

    private Boolean lessonFormatOnline;
    private Boolean lessonFormatOffline;
    private Boolean lessonFormatHybrid;

    @Size(max = 100)
    private String city;

    @Size(max = 255)
    private String addressText;

    @Size(max = 500)
    private String meetingLink;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal pricePerHour;

    private Boolean isPublic;

    @Size(max = 500)
    @Pattern(regexp = "^(https?://(www\\.)?instagram\\.com/.+)?$", message = "Invalid Instagram URL")
    private String instagramUrl;

    @Size(max = 100)
    @Pattern(regexp = "^(@?[a-zA-Z0-9_]{1,32})?$", message = "Invalid Telegram username")
    private String telegramUsername;

    @Size(max = 255)
    @Pattern(regexp = "^([a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,})?$", message = "Invalid email format")
    private String publicEmail;

    public UpdateMentorProfileRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }


    public String getHeadline() {
        return headline;
    }

    public String getBio() {
        return bio;
    }

    public String getSpecialization() {
        return specialization;
    }

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public Boolean getLessonFormatOnline() {
        return lessonFormatOnline;
    }

    public Boolean getLessonFormatOffline() {
        return lessonFormatOffline;
    }

    public Boolean getLessonFormatHybrid() {
        return lessonFormatHybrid;
    }

    public String getCity() {
        return city;
    }

    public String getAddressText() {
        return addressText;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setYearsExperience(Integer yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public void setLessonFormatOnline(Boolean lessonFormatOnline) {
        this.lessonFormatOnline = lessonFormatOnline;
    }

    public void setLessonFormatOffline(Boolean lessonFormatOffline) {
        this.lessonFormatOffline = lessonFormatOffline;
    }

    public void setLessonFormatHybrid(Boolean lessonFormatHybrid) {
        this.lessonFormatHybrid = lessonFormatHybrid;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getTelegramUsername() {
        return telegramUsername;
    }

    public void setTelegramUsername(String telegramUsername) {
        this.telegramUsername = telegramUsername;
    }

    public String getPublicEmail() {
        return publicEmail;
    }

    public void setPublicEmail(String publicEmail) {
        this.publicEmail = publicEmail;
    }
}