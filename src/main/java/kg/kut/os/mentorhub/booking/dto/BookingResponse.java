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
    private String mentorFirstName;
    private String mentorLastName;
    private String mentorAvatarUrl;
    private String studentFirstName;
    private String studentLastName;
    private String studentAvatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public String getMentorFirstName() {
        return mentorFirstName;
    }

    public String getMentorLastName() {
        return mentorLastName;
    }

    public String getMentorAvatarUrl() {
        return mentorAvatarUrl;
    }

    public String getStudentFirstName() {
        return studentFirstName;
    }

    public String getStudentLastName() {
        return studentLastName;
    }

    public String getStudentAvatarUrl() {
        return studentAvatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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

    public void setMentorFirstName(String mentorFirstName) {
        this.mentorFirstName = mentorFirstName;
    }

    public void setMentorLastName(String mentorLastName) {
        this.mentorLastName = mentorLastName;
    }

    public void setMentorAvatarUrl(String mentorAvatarUrl) {
        this.mentorAvatarUrl = mentorAvatarUrl;
    }

    public void setStudentFirstName(String studentFirstName) {
        this.studentFirstName = studentFirstName;
    }

    public void setStudentLastName(String studentLastName) {
        this.studentLastName = studentLastName;
    }

    public void setStudentAvatarUrl(String studentAvatarUrl) {
        this.studentAvatarUrl = studentAvatarUrl;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}