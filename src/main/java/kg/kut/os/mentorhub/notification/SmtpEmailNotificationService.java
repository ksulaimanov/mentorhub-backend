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
    public void sendPasswordResetCode(String toEmail, String code, String locale) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        boolean isRussian = "ru".equals(locale);
        message.setSubject(isRussian ? "Сброс пароля в MentorHub" : "MentorHub'та сырсөздү калыбына келтирүү");
        message.setText(isRussian ? buildPasswordResetTextRu(code) : buildPasswordResetTextKy(code));

        try {
            mailSender.send(message);
            log.info("Password reset email sent to {} (locale={})", toEmail, locale);
        } catch (MailException ex) {
            log.error("Failed to send password reset email to {}", toEmail, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось отправить письмо для сброса пароля"
            );
        }
    }

    private String buildPasswordResetTextRu(String code) {
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

    private String buildPasswordResetTextKy(String code) {
        return """
            Саламатсызбы!

            MentorHub'та сырсөздү калыбына келтирүү кодуңуз:

            %s

            Код 15 мүнөт иштейт.

            Эгер сиз сырсөздү калыбына келтирүүнү сурабасаңыз, бул катты этибарга албаңыз.

            Урмат менен,
            MentorHub
            """.formatted(code);
    }

    @Override
    public void sendApplicationApproved(String toEmail, String userName, String locale) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        boolean isRussian = "ru".equals(locale);
        message.setSubject(isRussian ? "Ваша заявка на менторство одобрена!" : "Менторлукка арызыңыз кабыл алынды!");
        message.setText(isRussian ? buildApplicationApprovedTextRu(userName) : buildApplicationApprovedTextKy(userName));

        try {
            mailSender.send(message);
            log.info("Application approved email sent to {} (locale={})", toEmail, locale);
        } catch (MailException ex) {
            log.error("Failed to send application approved email to {}", toEmail, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось отправить письмо об одобрении заявки"
            );
        }
    }

    private String buildApplicationApprovedTextRu(String userName) {
        return """
            Здравствуйте, %s!

            Отличные новости! Ваша заявка на менторство в MentorHub одобрена!

            Вы теперь полноценный ментор на платформе. Можно приступать к:
            - Заполнению профиля ментора
            - Установке доступных слотов
            - Публикации вашего профиля

            Добро пожаловать в команду менторов MentorHub!

            С уважением,
            MentorHub
            """.formatted(userName);
    }

    private String buildApplicationApprovedTextKy(String userName) {
        return """
            Саламатсызбы, %s!

            Жакшы жаңылык! MentorHub'та менторлукка арызыңыз кабыл алынды!

            Сиз эми платформанын толук укуктуу менторусуз. Баштасаңыз болот:
            - Ментор профилин толтуруу
            - Жеткиликтүү убакытты белгилөө
            - Профилиңизди жарыялоо

            MentorHub менторлор командасына кош келиңиз!

            Урмат менен,
            MentorHub
            """.formatted(userName);
    }

    @Override
    public void sendApplicationRejected(String toEmail, String userName, String rejectionReason, String locale) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        boolean isRussian = "ru".equals(locale);
        message.setSubject(isRussian ? "Результат рассмотрения заявки на менторство" : "Менторлукка арызды кароонун жыйынтыгы");
        message.setText(isRussian
                ? buildApplicationRejectedTextRu(userName, rejectionReason)
                : buildApplicationRejectedTextKy(userName, rejectionReason));

        try {
            mailSender.send(message);
            log.info("Application rejected email sent to {} (locale={})", toEmail, locale);
        } catch (MailException ex) {
            log.error("Failed to send application rejected email to {}", toEmail, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось отправить письмо об отклонении заявки"
            );
        }
    }

    private String buildApplicationRejectedTextRu(String userName, String rejectionReason) {
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

    private String buildApplicationRejectedTextKy(String userName, String rejectionReason) {
        return """
            Саламатсызбы, %s!

            MentorHub'та менторлукка кызыгууңуз үчүн рахмат. Тилекке каршы, арызыңыз кабыл алынган жок.

            Баш тартуу себеби:
            %s

            Кийинчерээк жаңы арыз бере аласыз же кошумча маалымат алуу үчүн биздин команда менен байланышсаңыз болот.

            Урмат менен,
            MentorHub
            """.formatted(userName, rejectionReason);
    }
}