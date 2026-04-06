package kg.kut.os.mentorhub.mentor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MentorProfileResponse {

    private Long id;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;
    private String avatarKey;
    private String avatarUrl;
    private String headline;
    private String bio;
    private String specialization;
    private Integer yearsExperience;
    private boolean lessonFormatOnline;
    private boolean lessonFormatOffline;
    private boolean lessonFormatHybrid;
    private String city;
    private String addressText;
    private String meetingLink;
    private BigDecimal pricePerHour;
    private BigDecimal averageRating;
    private Integer lessonsCompleted;
    private int reviewCount;
    private boolean verified;
    private boolean isPublic;
    private LocalDateTime createdAt;
    private String memberSince;
    private String preferredLocale;
    private String instagramUrl;
    private String telegramUsername;
    private String publicEmail;
    private boolean profileComplete;
    private int profileCompletenessPercent;
    private List<String> missingFields;

    public MentorProfileResponse() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarKey() {
        return avatarKey;
    }

    public String getAvatarUrl() {
        return avatarUrl;
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

    public String getAddressText() {
        return addressText;
    }

    public String getMeetingLink() {
        return meetingLink;
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

    public int getReviewCount() {
        return reviewCount;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
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

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }

    public String getPreferredLocale() {
        return preferredLocale;
    }

    public void setPreferredLocale(String preferredLocale) {
        this.preferredLocale = preferredLocale;
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

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }

    public int getProfileCompletenessPercent() {
        return profileCompletenessPercent;
    }

    public void setProfileCompletenessPercent(int profileCompletenessPercent) {
        this.profileCompletenessPercent = profileCompletenessPercent;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields;
    }
}