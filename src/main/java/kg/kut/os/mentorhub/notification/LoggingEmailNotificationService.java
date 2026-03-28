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
    public void sendEmailVerificationCode(String toEmail, String code, String locale) {
        log.info("MAIL DISABLED. Verification code for {} (locale={}): {}", toEmail, locale, code);
    }

    @Override
    public void sendPasswordResetCode(String toEmail, String code, String locale) {
        log.info("MAIL DISABLED. Password reset code for {} (locale={}): {}", toEmail, locale, code);
    }

    @Override
    public void sendApplicationApproved(String toEmail, String userName, String locale) {
        log.info("MAIL DISABLED. Application approved email for {} (locale={}): {}", toEmail, locale, userName);
    }

    @Override
    public void sendApplicationRejected(String toEmail, String userName, String rejectionReason, String locale) {
        log.info("MAIL DISABLED. Application rejected email for {} (locale={}): {} - Reason: {}", toEmail, locale, userName, rejectionReason);
    }
}