package kg.kut.os.mentorhub.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true", matchIfMissing = true)
public class SmtpEmailNotificationService implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public SmtpEmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendEmailVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Подтверждение email в MentorHub");
        message.setText(buildVerificationText(code));

        try {
            mailSender.send(message);
            log.info("Verification email sent to {}", toEmail);
        } catch (MailException ex) {
            log.error("Failed to send verification email to {}", toEmail, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось отправить письмо с кодом подтверждения"
            );
        }
    }

    private String buildVerificationText(String code) {
        return """
                Здравствуйте!

                Ваш код подтверждения для MentorHub:

                %s

                Код действует 15 минут.

                Если вы не регистрировались в MentorHub, просто проигнорируйте это письмо.

                С уважением,
                MentorHub
                """.formatted(code);
    }

    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Сброс пароля в MentorHub");
        message.setText(buildPasswordResetText(code));

        try {
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MailException ex) {
            log.error("Failed to send password reset email to {}", toEmail, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось отправить письмо для сброса пароля"
            );
        }
    }

    private String buildPasswordResetText(String code) {
        return """
            Здравствуйте!

            Ваш код для сброса пароля в MentorHub:

            %s

            Код действует 15 минут.

            Если вы не запрашивали сброс пароля, просто проигнорируйте это письмо.

            С уважением,
            MentorHub
            """.formatted(code);
    }
}