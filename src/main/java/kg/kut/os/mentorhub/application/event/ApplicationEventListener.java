package kg.kut.os.mentorhub.application.event;

import kg.kut.os.mentorhub.notification.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ApplicationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventListener.class);
    private final EmailNotificationService emailNotificationService;

    public ApplicationEventListener(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApplicationApproved(ApplicationApprovedEvent event) {
        log.info("Dispatching async approval email to [{}] AFTER_COMMIT", event.getApplicantEmail());
        try {
            emailNotificationService.sendApplicationApproved(event.getApplicantEmail(), event.getApplicantEmail(), event.getPreferredLocale());
        } catch (Exception e) {
            log.error("Failed to send approval email to [{}]", event.getApplicantEmail(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApplicationRejected(ApplicationRejectedEvent event) {
        log.info("Dispatching async rejection email to [{}] AFTER_COMMIT", event.getApplicantEmail());
        try {
            emailNotificationService.sendApplicationRejected(event.getApplicantEmail(), event.getApplicantEmail(), event.getRejectionReason(), event.getPreferredLocale());
        } catch (Exception e) {
            log.error("Failed to send rejection email to [{}]", event.getApplicantEmail(), e);
        }
    }
}
