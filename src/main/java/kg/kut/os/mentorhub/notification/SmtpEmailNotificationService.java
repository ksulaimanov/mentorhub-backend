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
    public void sendEmailVerificationCode(String toEmail, String code, String locale) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        boolean isRussian = "ru".equals(locale);
        message.setSubject(isRussian ? "Подтверждение email в MentorHub" : "MentorHub'та email тастыктоо");
        message.setText(isRussian ? buildVerificationTextRu(code) : buildVerificationTextKy(code));

        try {
            mailSender.send(message);
            log.info("Verification email sent to {} (locale={})", toEmail, locale);
        } catch (MailException ex) {
            log.error("Failed to send verification email to {}", toEmail, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось отправить письмо с кодом подтверждения"
            );
        }
    }

    private String buildVerificationTextRu(String code) {
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

    private String buildVerificationTextKy(String code) {
        return """
                Саламатсызбы!

                MentorHub үчүн тастыктоо кодуңуз:

                %s

                Код 15 мүнөт иштейт.

                Эгер сиз MentorHub'ка катталбасаңыз, бул катты этибарга албаңыз.

                Урмат менен,
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

    @Override
    public void sendApplicationApproved(String toEmail, String userName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Ваша заявка на менторство одобрена!");
        message.setText(buildApplicationApprovedText(userName));

        try {
            mailSender.send(message);
            log.info("Application approved email sent to {}", toEmail);
        } catch (MailException ex) {
            log.error("Failed to send application approved email to {}", toEmail, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось отправить письмо об одобрении заявки"
            );
        }
    }

    private String buildApplicationApprovedText(String userName) {
        return """
            Здравствуйте, %s!

            Отлично новости! Ваша заявка на менторство в MentorHub одобрена!

            Вы теперь полноценный ментор на платформе. Можно приступать к:
            - Заполнению профиля ментора
            - Установке доступных слотов
            - Публикации вашего профиля

            Добро пожаловать в команду менторов MentorHub!

            С уважением,
            MentorHub
            """.formatted(userName);
    }

    @Override
    public void sendApplicationRejected(String toEmail, String userName, String rejectionReason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Результат рассмотрения заявки на менторство");
        message.setText(buildApplicationRejectedText(userName, rejectionReason));

        try {
            mailSender.send(message);
            log.info("Application rejected email sent to {}", toEmail);
        } catch (MailException ex) {
            log.error("Failed to send application rejected email to {}", toEmail, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось отправить письмо об отклонении заявки"
            );
        }
    }

    private String buildApplicationRejectedText(String userName, String rejectionReason) {
        return """
            Здравствуйте, %s!

            Спасибо за интерес к менторству в MentorHub. К сожалению, ваша заявка не была одобрена.

            Причина отклонения:
            %s

            Вы можете подать новую заявку позже или связаться с нашей командой для получения дополнительной информации.

            С уважением,
            MentorHub
            """.formatted(userName, rejectionReason);
    }
}