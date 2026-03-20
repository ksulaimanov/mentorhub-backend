package kg.kut.os.mentorhub.mentor.dto;

import java.math.BigDecimal;

public class MentorDirectoryItemResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String avatarKey;
    private String headline;
    private String specialization;
    private Integer yearsExperience;
    private boolean lessonFormatOnline;
    private boolean lessonFormatOffline;
    private boolean lessonFormatHybrid;
    private String city;
    private BigDecimal pricePerHour;
    private BigDecimal averageRating;
    private Integer lessonsCompleted;
    private boolean verified;

    public MentorDirectoryItemResponse() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
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

    public String getHeadline() {
        return headline;
    }

    public String getSpecialization() {
        return specialization;
    }

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public boolean isLessonFormatOnline() {
        return lessonFormatOnline;
    }

    public boolean isLessonFormatOffline() {
        return lessonFormatOffline;
    }

    public boolean isLessonFormatHybrid() {
        return lessonFormatHybrid;
    }

    public String getCity() {
        return city;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public Integer getLessonsCompleted() {
        return lessonsCompleted;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setYearsExperience(Integer yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public void setLessonFormatOnline(boolean lessonFormatOnline) {
        this.lessonFormatOnline = lessonFormatOnline;
    }

    public void setLessonFormatOffline(boolean lessonFormatOffline) {
        this.lessonFormatOffline = lessonFormatOffline;
    }

    public void setLessonFormatHybrid(boolean lessonFormatHybrid) {
        this.lessonFormatHybrid = lessonFormatHybrid;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public void setLessonsCompleted(Integer lessonsCompleted) {
        this.lessonsCompleted = lessonsCompleted;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}