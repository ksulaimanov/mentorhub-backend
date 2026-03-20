package kg.kut.os.mentorhub.availability.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kg.kut.os.mentorhub.availability.entity.LessonFormat;

import java.time.LocalDateTime;

public class CreateAvailabilitySlotRequest {

    @NotNull
    @Future
    private LocalDateTime startAt;

    @NotNull
    @Future
    private LocalDateTime endAt;

    @NotBlank
    @Size(max = 100)
    private String timezone;

    @NotNull
    private LessonFormat lessonFormat;

    @Size(max = 500)
    private String meetingLink;

    @Size(max = 255)
    private String addressText;

    public CreateAvailabilitySlotRequest() {
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
}