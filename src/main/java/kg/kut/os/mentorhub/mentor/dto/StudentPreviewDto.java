package kg.kut.os.mentorhub.mentor.dto;

import java.util.List;

public class StudentPreviewDto {
    private Long studentUserId;
    private String firstName;
    private String lastName;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private String city;
    private String timezone;

    // Private fields visible only to mentor who has bookings with student
    private String email;
    private String phone;

    // Stats
    private long totalCompletedLessons;
    private long totalCancelledLessons;
    private int totalLearningHours;

    // History with THIS mentor
    private List<BookingHistoryDto> interactionHistory;

    public StudentPreviewDto() {
    }

    public Long getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(Long studentUserId) {
        this.studentUserId = studentUserId;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getTotalCompletedLessons() {
        return totalCompletedLessons;
    }

    public void setTotalCompletedLessons(long totalCompletedLessons) {
        this.totalCompletedLessons = totalCompletedLessons;
    }

    public long getTotalCancelledLessons() {
        return totalCancelledLessons;
    }

    public void setTotalCancelledLessons(long totalCancelledLessons) {
        this.totalCancelledLessons = totalCancelledLessons;
    }

    public int getTotalLearningHours() {
        return totalLearningHours;
    }

    public void setTotalLearningHours(int totalLearningHours) {
        this.totalLearningHours = totalLearningHours;
    }

    public List<BookingHistoryDto> getInteractionHistory() {
        return interactionHistory;
    }

    public void setInteractionHistory(List<BookingHistoryDto> interactionHistory) {
        this.interactionHistory = interactionHistory;
    }
}

