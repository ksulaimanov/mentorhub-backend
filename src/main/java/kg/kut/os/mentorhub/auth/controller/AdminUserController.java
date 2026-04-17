package kg.kut.os.mentorhub.auth.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.auth.dto.AdminUserSummaryDto;
import kg.kut.os.mentorhub.auth.dto.ChangeStatusRequest;
import kg.kut.os.mentorhub.auth.service.AdminUserService;
import kg.kut.os.mentorhub.common.dto.MessageResponse;
import kg.kut.os.mentorhub.review.dto.ReviewResponse;
import kg.kut.os.mentorhub.review.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final ReviewService reviewService;

    public AdminUserController(AdminUserService adminUserService, ReviewService reviewService) {
        this.adminUserService = adminUserService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminUserSummaryDto>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getUsers(pageable));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request
    ) {
        adminUserService.changeStatus(id, request.getStatus());
        return ResponseEntity.ok(new MessageResponse("Статус пользователя успешно обновлен"));
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<MessageResponse> deleteAvatar(@PathVariable Long id) {
        adminUserService.deleteAvatar(id);
        return ResponseEntity.ok(new MessageResponse("Аватар пользователя успешно удален"));
    }

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @RequestParam(required = false, defaultValue = "false") boolean lowRatingOnly,
            Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.getAdminReviews(lowRatingOnly, pageable));
    }
}
