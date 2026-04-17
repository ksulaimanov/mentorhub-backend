package kg.kut.os.mentorhub.notification.controller;

import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.common.security.CurrentUser;
import kg.kut.os.mentorhub.notification.dto.NotificationDto;
import kg.kut.os.mentorhub.notification.service.InAppNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final InAppNotificationService notificationService;

    public NotificationController(InAppNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @CurrentUser User currentUser,
            Pageable pageable
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(currentUser.getId(), pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@CurrentUser User currentUser) {
        return ResponseEntity.ok(notificationService.getUnreadCount(currentUser.getId()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @CurrentUser User currentUser,
            @PathVariable Long id
    ) {
        notificationService.markAsRead(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@CurrentUser User currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}

