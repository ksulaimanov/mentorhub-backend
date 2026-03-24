package kg.kut.os.mentorhub.dashboard.controller;

import kg.kut.os.mentorhub.dashboard.dto.AdminDashboardDto;
import kg.kut.os.mentorhub.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * GET /api/admin/dashboard
     * Returns aggregate statistics and 10 nearest upcoming events.
     * Access: ROLE_ADMIN only (enforced by @PreAuthorize; /api/admin/** is
     * covered by anyRequest().authenticated() in SecurityConfig + this annotation).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardDto> getDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }
}

