package kg.kut.os.mentorhub.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;

public class UpdateBookingStatusRequest {

    @NotNull
    private BookingStatus status;

    @Size(max = 1000)
    private String mentorNote;

    public UpdateBookingStatusRequest() {
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getMentorNote() {
        return mentorNote;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setMentorNote(String mentorNote) {
        this.mentorNote = mentorNote;
    }
}