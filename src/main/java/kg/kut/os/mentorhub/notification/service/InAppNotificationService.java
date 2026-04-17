package kg.kut.os.mentorhub.notification.service;

import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.auth.repository.UserRepository;
import kg.kut.os.mentorhub.common.exception.NotFoundException;
import kg.kut.os.mentorhub.notification.dto.NotificationDto;
import kg.kut.os.mentorhub.notification.entity.Notification;
import kg.kut.os.mentorhub.notification.entity.NotificationType;
import kg.kut.os.mentorhub.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InAppNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public InAppNotificationService(NotificationRepository notificationRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void createNotification(Long recipientId, NotificationType type, String title, String content, String actionUrl) {
        User recipient = userRepository.findById(recipientId).orElse(null);
        if (recipient == null) return;

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setActionUrl(actionUrl);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);

        // Push notification in real-time
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, mapToDto(saved));
        } catch (Exception e) {
            // Ignore if broker fails
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadNotifications(userId);
    }

    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Уведомление не найдено"));

        if (notification.getRecipient().getId().equals(userId)) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByRecipientId(userId);
    }

    private NotificationDto mapToDto(Notification notif) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notif.getId());
        dto.setType(notif.getType());
        dto.setTitle(notif.getTitle());
        dto.setContent(notif.getContent());
        dto.setRead(notif.isRead());
        dto.setActionUrl(notif.getActionUrl());
        dto.setCreatedAt(notif.getCreatedAt());
        return dto;
    }
}
