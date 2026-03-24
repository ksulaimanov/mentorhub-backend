package kg.kut.os.mentorhub.dashboard.controller;

import kg.kut.os.mentorhub.common.security.CurrentUserService;
import kg.kut.os.mentorhub.dashboard.dto.StudentDashboardDto;
import kg.kut.os.mentorhub.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/dashboard")
public class StudentDashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    public StudentDashboardController(
            DashboardService dashboardService,
            CurrentUserService currentUserService
    ) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    /** GET /api/student/dashboard — access: ROLE_STUDENT (SecurityConfig path rule) */
    @GetMapping
    public ResponseEntity<StudentDashboardDto> getDashboard() {
        return ResponseEntity.ok(
                dashboardService.getStudentDashboard(currentUserService.getCurrentUserId())
        );
    }
}
