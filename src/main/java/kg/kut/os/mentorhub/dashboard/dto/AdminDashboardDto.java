package kg.kut.os.mentorhub.dashboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminDashboardDto {
    private DashboardStatsDto stats;
    private List<UpcomingEventDto> recentEvents;
}