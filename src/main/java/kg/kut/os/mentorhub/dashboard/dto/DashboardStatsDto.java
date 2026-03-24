package kg.kut.os.mentorhub.dashboard.dto;

import lombok.Data;

@Data
public class DashboardStatsDto {
    private long totalBookings;
    private long completedBookings;
    private long upcomingBookings;
    private long totalStudents;
    private long totalMentors;
    private double averageRating;
}