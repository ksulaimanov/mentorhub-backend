package kg.kut.os.mentorhub.application.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.application.dto.ApplicationStatusResponse;
import kg.kut.os.mentorhub.application.dto.SubmitApplicationRequest;
import kg.kut.os.mentorhub.application.service.MentorApplicationService;
import kg.kut.os.mentorhub.common.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/mentor-application")
@PreAuthorize("hasRole('STUDENT')")
public class StudentApplicationController {

    private final MentorApplicationService mentorApplicationService;

    public StudentApplicationController(
            MentorApplicationService mentorApplicationService
    ) {
        this.mentorApplicationService = mentorApplicationService;
    }

    /**
     * Студент подаёт заявку на менторство
     * POST /api/student/mentor-application
     */
    @PostMapping
    public ResponseEntity<ApplicationStatusResponse> submitApplication(
            @Valid @RequestBody SubmitApplicationRequest request,
            @CurrentUser Long userId
    ) {
        ApplicationStatusResponse response = mentorApplicationService.submitApplication(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Студент получает статус своей заявки
     * GET /api/student/mentor-application
     */
    @GetMapping
    public ResponseEntity<ApplicationStatusResponse> getApplicationStatus(
            @CurrentUser Long userId
    ) {
        ApplicationStatusResponse response = mentorApplicationService.getApplicationStatus(userId);
        return ResponseEntity.ok(response);
    }
}

