package kg.kut.os.mentorhub.booking.dto;

import jakarta.validation.constraints.Size;

/**
 * Optional request body for mentor booking action endpoints
 * (confirm, decline, complete). Only carries an optional note.
 */
public class MentorBookingActionRequest {

    @Size(max = 1000)
    private String mentorNote;

    public MentorBookingActionRequest() {
    }

    public String getMentorNote() {
        return mentorNote;
    }

    public void setMentorNote(String mentorNote) {
        this.mentorNote = mentorNote;
    }
}

