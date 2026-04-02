package kg.kut.os.mentorhub.mentor.dto;

import kg.kut.os.mentorhub.review.dto.ReviewResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PublicMentorProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String headline;
    private String bio;
    private String specialization;
    private Integer yearsExperience;
    private boolean lessonFormatOnline;
    private boolean lessonFormatOffline;
    private boolean lessonFormatHybrid;
    private String city;
    private BigDecimal pricePerHour;
    private BigDecimal averageRating;
    private Integer lessonsCompleted;
    private int reviewCount;
    private LocalDateTime memberSince;
    private boolean verified;
    private boolean hasAvailableSlots;
    private List<ReviewResponse> latestReviews;

    public PublicMentorProfileResponse() {
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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

    public LocalDateTime getMemberSince() {
        return memberSince;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isHasAvailableSlots() {
        return hasAvailableSlots;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public void setMemberSince(LocalDateTime memberSince) {
        this.memberSince = memberSince;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setHasAvailableSlots(boolean hasAvailableSlots) {
        this.hasAvailableSlots = hasAvailableSlots;
    }

    public List<ReviewResponse> getLatestReviews() {
        return latestReviews;
    }

    public void setLatestReviews(List<ReviewResponse> latestReviews) {
        this.latestReviews = latestReviews;
    }
}