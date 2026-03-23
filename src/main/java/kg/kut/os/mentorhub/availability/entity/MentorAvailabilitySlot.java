package kg.kut.os.mentorhub.availability.entity;

import jakarta.persistence.*;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_availability_slots")
public class MentorAvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private MentorProfile mentor;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false, length = 100)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_format", nullable = false, length = 20)
    private LessonFormat lessonFormat;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "address_text", length = 255)
    private String addressText;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (capacity == null || capacity < 1) {
            capacity = 1;
        }

        if (!isActive) {
            isActive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public MentorAvailabilitySlot() {
    }

    public Long getId() {
        return id;
    }

    public MentorProfile getMentor() {
        return mentor;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public String getTimezone() {
        return timezone;
    }

    public LessonFormat getLessonFormat() {
        return lessonFormat;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public String getAddressText() {
        return addressText;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setMentor(MentorProfile mentor) {
        this.mentor = mentor;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setLessonFormat(LessonFormat lessonFormat) {
        this.lessonFormat = lessonFormat;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}