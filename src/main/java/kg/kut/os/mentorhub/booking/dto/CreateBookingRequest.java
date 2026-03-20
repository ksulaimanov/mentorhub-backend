package kg.kut.os.mentorhub.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateBookingRequest {

    @NotNull
    private Long availabilitySlotId;

    @Size(max = 1000)
    private String studentNote;

    public CreateBookingRequest() {
    }

    public Long getAvailabilitySlotId() {
        return availabilitySlotId;
    }

    public String getStudentNote() {
        return studentNote;
    }

    public void setAvailabilitySlotId(Long availabilitySlotId) {
        this.availabilitySlotId = availabilitySlotId;
    }

    public void setStudentNote(String studentNote) {
        this.studentNote = studentNote;
    }
}