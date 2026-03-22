package kg.kut.os.mentorhub.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingEmailNotificationService implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailNotificationService.class);

    @Override
    public void sendEmailVerificationCode(String toEmail, String code) {
        log.info("Email verification code for {}: {}", toEmail, code);
    }
}