package kg.kut.os.mentorhub.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false")
public class LoggingEmailNotificationService implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailNotificationService.class);

    @Override
    public void sendEmailVerificationCode(String toEmail, String code) {
        log.info("MAIL DISABLED. Verification code for {}: {}", toEmail, code);
    }

    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        log.info("MAIL DISABLED. Password reset code for {}: {}", toEmail, code);
    }

    @Override
    public void sendApplicationApproved(String toEmail, String userName) {
        log.info("MAIL DISABLED. Application approved email for {}: {}", toEmail, userName);
    }

    @Override
    public void sendApplicationRejected(String toEmail, String userName, String rejectionReason) {
        log.info("MAIL DISABLED. Application rejected email for {}: {} - Reason: {}", toEmail, userName, rejectionReason);
    }
}