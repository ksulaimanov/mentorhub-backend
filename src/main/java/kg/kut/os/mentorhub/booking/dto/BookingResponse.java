package kg.kut.os.mentorhub.booking.dto;

import kg.kut.os.mentorhub.availability.entity.LessonFormat;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;

import java.time.LocalDateTime;

public class BookingResponse {

    private Long id;
    private Long studentId;
    private Long mentorId;
    private Long availabilitySlotId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String timezone;
    private LessonFormat lessonFormat;
    private String meetingLink;
    private String addressText;
    private BookingStatus status;
    private String studentNote;
    private String mentorNote;

    public BookingResponse() {
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getMentorId() {
        return mentorId;
    }

    public Long getAvailabilitySlotId() {
        return availabilitySlotId;
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

    public BookingStatus getStatus() {
        return status;
    }

    public String getStudentNote() {
        return studentNote;
    }

    public String getMentorNote() {
        return mentorNote;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public void setAvailabilitySlotId(Long availabilitySlotId) {
        this.availabilitySlotId = availabilitySlotId;
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

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setStudentNote(String studentNote) {
        this.studentNote = studentNote;
    }

    public void setMentorNote(String mentorNote) {
        this.mentorNote = mentorNote;
    }
}