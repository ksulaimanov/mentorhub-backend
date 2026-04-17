package kg.kut.os.mentorhub.dashboard.dto;

import lombok.Data;

import java.util.List;

@Data
public class MentorDashboardDto {
    private String displayName;
    private String avatarUrl;
    private boolean profileComplete;
    private int profileCompletenessPercent;
    private List<String> missingFields;
    private String memberSince;
    private List<UpcomingEventDto> upcomingEvents;
    private long totalBookings;
    private long completedBookings;
    private long totalStudents;
    private double averageRating;
}