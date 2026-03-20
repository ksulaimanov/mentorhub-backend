package kg.kut.os.mentorhub.availability.dto;

import kg.kut.os.mentorhub.availability.entity.LessonFormat;

import java.time.LocalDateTime;

public class AvailabilitySlotResponse {

    private Long id;
    private Long mentorId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String timezone;
    private LessonFormat lessonFormat;
    private String meetingLink;
    private String addressText;
    private boolean active;

    public AvailabilitySlotResponse() {
    }

    public Long getId() {
        return id;
    }

    public Long getMentorId() {
        return mentorId;
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

    public boolean isActive() {
        return active;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
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

    public void setActive(boolean active) {
        this.active = active;
    }
}