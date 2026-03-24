package kg.kut.os.mentorhub.dashboard.dto;

import lombok.Data;

import java.util.List;

@Data
public class MentorDashboardDto {
    private List<UpcomingEventDto> upcomingEvents;
    private long totalBookings;
    private long completedBookings;
    private long totalStudents;
    private double averageRating;
}