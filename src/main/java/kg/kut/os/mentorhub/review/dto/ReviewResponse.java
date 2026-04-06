package kg.kut.os.mentorhub.review.dto;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long id;
    private Long bookingId;
    private Long mentorId;
    private Long studentId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String studentFirstName;
    private String studentLastName;
    private String studentAvatarUrl;

    public ReviewResponse() {
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getMentorId() {
        return mentorId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStudentFirstName() {
        return studentFirstName;
    }

    public void setStudentFirstName(String studentFirstName) {
        this.studentFirstName = studentFirstName;
    }

    public String getStudentLastName() {
        return studentLastName;
    }

    public void setStudentLastName(String studentLastName) {
        this.studentLastName = studentLastName;
    }

    public String getStudentAvatarUrl() {
        return studentAvatarUrl;
    }

    public void setStudentAvatarUrl(String studentAvatarUrl) {
        this.studentAvatarUrl = studentAvatarUrl;
    }
}