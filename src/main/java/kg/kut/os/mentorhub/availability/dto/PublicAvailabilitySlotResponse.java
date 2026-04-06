package kg.kut.os.mentorhub.availability.dto;

import kg.kut.os.mentorhub.availability.entity.LessonFormat;

import java.time.LocalDateTime;

/**
 * Public-safe availability slot response.
 * Does NOT expose meetingLink, addressText, or internal fields.
 * The frontend uses this to render booking CTA / time-picker.
 */
public class PublicAvailabilitySlotResponse {

    private Long id;
    private Long mentorId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String timezone;
    private LessonFormat lessonFormat;
    private Integer capacity;
    private Integer bookedCount;
    private Integer availableSeats;
    private boolean bookable;

    public PublicAvailabilitySlotResponse() {
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

    public Integer getCapacity() {
        return capacity;
    }

    public Integer getBookedCount() {
        return bookedCount;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public boolean isBookable() {
        return bookable;
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

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setBookedCount(Integer bookedCount) {
        this.bookedCount = bookedCount;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public void setBookable(boolean bookable) {
        this.bookable = bookable;
    }
}

