package kg.kut.os.mentorhub.mentor.dto;

import java.time.LocalDateTime;

public class BookingHistoryDto {
    private Long bookingId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
    private String topicOrNote;

    public BookingHistoryDto(Long bookingId, LocalDateTime startAt, LocalDateTime endAt, String status, String topicOrNote) {
        this.bookingId = bookingId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.topicOrNote = topicOrNote;
    }

    public BookingHistoryDto() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTopicOrNote() {
        return topicOrNote;
    }

    public void setTopicOrNote(String topicOrNote) {
        this.topicOrNote = topicOrNote;
    }
}

