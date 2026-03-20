package kg.kut.os.mentorhub.mentor.entity;

import jakarta.persistence.*;
import kg.kut.os.mentorhub.auth.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_profiles")
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "avatar_key", length = 255)
    private String avatarKey;

    @Column(length = 255)
    private String headline;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(length = 255)
    private String specialization;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "lesson_format_online", nullable = false)
    private boolean lessonFormatOnline;

    @Column(name = "lesson_format_offline", nullable = false)
    private boolean lessonFormatOffline;

    @Column(name = "lesson_format_hybrid", nullable = false)
    private boolean lessonFormatHybrid;

    @Column(length = 100)
    private String city;

    @Column(name = "address_text", length = 255)
    private String addressText;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "price_per_hour", precision = 12, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "lessons_completed", nullable = false)
    private Integer lessonsCompleted;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (averageRating == null) {
            averageRating = BigDecimal.ZERO;
        }
        if (lessonsCompleted == null) {
            lessonsCompleted = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public MentorProfile() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
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

    public boolean isVerified() {
        return verified;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setUser(User user) {
        this.user = user;
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

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}