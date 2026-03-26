package kg.kut.os.mentorhub.application.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.application.dto.AdminApplicationDetailView;
import kg.kut.os.mentorhub.application.dto.AdminApplicationView;
import kg.kut.os.mentorhub.application.dto.RejectApplicationRequest;
import kg.kut.os.mentorhub.application.entity.MentorApplicationStatus;
import kg.kut.os.mentorhub.application.service.MentorApplicationService;
import kg.kut.os.mentorhub.common.dto.MessageResponse;
import kg.kut.os.mentorhub.common.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/mentor-applications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApplicationController {

    private final MentorApplicationService mentorApplicationService;

    public AdminApplicationController(MentorApplicationService mentorApplicationService) {
        this.mentorApplicationService = mentorApplicationService;
    }

    /**
     * Получить список всех заявок на менторство
     * GET /api/admin/mentor-applications?status=PENDING&page=0&size=20
     */
    public ResponseEntity<Page<AdminApplicationView>> listApplications(
            @RequestParam(required = false) MentorApplicationStatus status,
            Pageable pageable
    ) {
        Page<AdminApplicationView> applications = mentorApplicationService.listApplications(status, pageable);
        return ResponseEntity.ok(applications);
    }

    /**
     * Получить детали одной заявки
     * GET /api/admin/mentor-applications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminApplicationDetailView> getApplicationDetail(
            @PathVariable Long id
    ) {
        AdminApplicationDetailView application = mentorApplicationService.getApplicationDetail(id);
        return ResponseEntity.ok(application);
    }

    /**
     * Одобрить заявку на менторство
     * POST /api/admin/mentor-applications/{id}/approve
     *
     * Действия:
     * 1. Обновить статус заявки на APPROVED
     * 2. Добавить роль ROLE_MENTOR пользователю
     * 3. Создать MentorProfile с verified=true, isPublic=false
     * 4. Отправить email ментору
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<MessageResponse> approveApplication(
            @PathVariable Long id,
            @CurrentUser Long adminUserId
    ) {
        mentorApplicationService.approveApplication(id, adminUserId);
        return ResponseEntity.ok(new MessageResponse("Заявка одобрена. Пользователю выдана роль ROLE_MENTOR"));
    }

    /**
     * Отклонить заявку на менторство
     * POST /api/admin/mentor-applications/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<MessageResponse> rejectApplication(
            @PathVariable Long id,
            @Valid @RequestBody RejectApplicationRequest request,
            @CurrentUser Long adminUserId
    ) {
        mentorApplicationService.rejectApplication(id, request.getRejectionReason(), adminUserId);
        return ResponseEntity.ok(new MessageResponse("Заявка отклонена. Уведомление отправлено пользователю"));
    }
}

