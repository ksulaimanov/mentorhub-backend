package kg.kut.os.mentorhub.dashboard.controller;

import kg.kut.os.mentorhub.common.security.CurrentUserService;
import kg.kut.os.mentorhub.dashboard.dto.MentorDashboardDto;
import kg.kut.os.mentorhub.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentor/dashboard")
public class MentorDashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    public MentorDashboardController(
            DashboardService dashboardService,
            CurrentUserService currentUserService
    ) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    /** GET /api/mentor/dashboard — access: ROLE_MENTOR (SecurityConfig path rule) */
    @GetMapping
    public ResponseEntity<MentorDashboardDto> getDashboard() {
        return ResponseEntity.ok(
                dashboardService.getMentorDashboard(currentUserService.getCurrentUserId())
        );
    }
}
