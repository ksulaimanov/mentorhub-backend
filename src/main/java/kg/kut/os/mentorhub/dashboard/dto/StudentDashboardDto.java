package kg.kut.os.mentorhub.dashboard.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentDashboardDto {
    private List<UpcomingEventDto> upcomingEvents;
    private long totalBookings;
    private long completedBookings;
}