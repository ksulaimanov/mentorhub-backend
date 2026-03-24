package kg.kut.os.mentorhub.dashboard.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UpcomingEventDto {
    private Long id;
    private String type;
    private String title;
    private String description;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String mentorName;
    private String studentName;
    private String lessonFormat;
    private String status;
    private Integer capacity;
    private Integer bookedCount;
    private Integer availableSeats;
}