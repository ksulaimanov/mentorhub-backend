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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true", matchIfMissing = true)
public class SmtpEmailNotificationService implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailNotificationService.class);
    private static final DateTimeFormatter SLOT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

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
        message.setSubject(isRussian ? "Подтверждение email в JaiMentorship" : "JaiMentorship'та email тастыктоо");
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

                Ваш код подтверждения для JaiMentorship:

                %s

                Код действует 15 минут.

                Если вы не регистрировались в JaiMentorship, просто проигнорируйте это письмо.

                С уважением,
                JaiMentorship
                """.formatted(code);
    }

    private String buildVerificationTextKy(String code) {
        return """
                Саламатсызбы!

                JaiMentorship үчүн тастыктоо кодуңуз:

                %s

                Код 15 мүнөт иштейт.

                Эгер сиз JaiMentorship'ка катталбасаңыз, бул катты этибарга албаңыз.

                Урмат менен,
                JaiMentorship
                """.formatted(code);
    }

    @Override
    public void sendPasswordResetCode(String toEmail, String code, String locale) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        boolean isRussian = "ru".equals(locale);
        message.setSubject(isRussian ? "Сброс пароля в JaiMentorship" : "JaiMentorship'та сырсөздү калыбына келтирүү");
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

            Ваш код для сброса пароля в JaiMentorship:

            %s

            Код действует 15 минут.

            Если вы не запрашивали сброс пароля, просто проигнорируйте это письмо.

            С уважением,
            JaiMentorship
            """.formatted(code);
    }

    private String buildPasswordResetTextKy(String code) {
        return """
            Саламатсызбы!

            JaiMentorship'та сырсөздү калыбына келтирүү кодуңуз:

            %s

            Код 15 мүнөт иштейт.

            Эгер сиз сырсөздү калыбына келтирүүнү сурабасаңыз, бул катты этибарга албаңыз.

            Урмат менен,
            JaiMentorship
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

            Отличные новости! Ваша заявка на менторство в JaiMentorship одобрена!

            Вы теперь полноценный ментор на платформе. Можно приступать к:
            - Заполнению профиля ментора
            - Установке доступных слотов
            - Публикации вашего профиля

            Добро пожаловать в команду менторов JaiMentorship!

            С уважением,
            JaiMentorship
            """.formatted(userName);
    }

    private String buildApplicationApprovedTextKy(String userName) {
        return """
            Саламатсызбы, %s!

            Жакшы жаңылык! JaiMentorship'та менторлукка арызыңыз кабыл алынды!

            Сиз эми платформанын толук укуктуу менторусуз. Баштасаңыз болот:
            - Ментор профилин толтуруу
            - Жеткиликтүү убакытты белгилөө
            - Профилиңизди жарыялоо

            JaiMentorship менторлор командасына кош келиңиз!

            Урмат менен,
            JaiMentorship
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

            Спасибо за интерес к менторству в JaiMentorship. К сожалению, ваша заявка не была одобрена.

            Причина отклонения:
            %s

            Вы можете подать новую заявку позже или связаться с нашей командой для получения дополнительной информации.

            С уважением,
            JaiMentorship
            """.formatted(userName, rejectionReason);
    }

    private String buildApplicationRejectedTextKy(String userName, String rejectionReason) {
        return """
            Саламатсызбы, %s!

            JaiMentorship'та менторлукка кызыгууңуз үчүн рахмат. Тилекке каршы, арызыңыз кабыл алынган жок.

            Баш тартуу себеби:
            %s

            Кийинчерээк жаңы арыз бере аласыз же кошумча маалымат алуу үчүн биздин команда менен байланышсаңыз болот.

            Урмат менен,
            JaiMentorship
            """.formatted(userName, rejectionReason);
    }

    // ----------------------------------------------------------------
    // Booking lifecycle notifications
    // ----------------------------------------------------------------

    @Override
    public void sendBookingCreated(String toMentorEmail, String studentName, LocalDateTime startAt, LocalDateTime endAt, String locale) {
        boolean ru = "ru".equals(locale);
        String subject = ru ? "Новая запись на занятие — JaiMentorship" : "Жаңы сабакка жазуу — JaiMentorship";
        String time = formatSlot(startAt, endAt);
        String body = ru
                ? "Здравствуйте!\n\nУ вас новая запись на занятие.\n\nУченик: %s\nВремя: %s\n\nПожалуйста, подтвердите или отклоните запись в личном кабинете.\n\nС уважением,\nJaiMentorship".formatted(studentName, time)
                : "Саламатсызбы!\n\nСабакка жаңы жазуу бар.\n\nОкуучу: %s\nУбакыт: %s\n\nЖеке кабинетиңизде тастыктаңыз же баш тартыңыз.\n\nУрмат менен,\nJaiMentorship".formatted(studentName, time);
        sendQuietly(toMentorEmail, subject, body, "booking-created");
    }

    @Override
    public void sendBookingConfirmed(String toStudentEmail, String mentorName, LocalDateTime startAt, LocalDateTime endAt, String locale) {
        boolean ru = "ru".equals(locale);
        String subject = ru ? "Ваша запись подтверждена — JaiMentorship" : "Жазууңуз тастыкталды — JaiMentorship";
        String time = formatSlot(startAt, endAt);
        String body = ru
                ? "Здравствуйте!\n\nВаша запись на занятие подтверждена.\n\nМентор: %s\nВремя: %s\n\nС уважением,\nJaiMentorship".formatted(mentorName, time)
                : "Саламатсызбы!\n\nСабакка жазууңуз тастыкталды.\n\nМентор: %s\nУбакыт: %s\n\nУрмат менен,\nJaiMentorship".formatted(mentorName, time);
        sendQuietly(toStudentEmail, subject, body, "booking-confirmed");
    }

    @Override
    public void sendBookingCancelled(String toEmail, String otherPartyName, LocalDateTime startAt, LocalDateTime endAt, String locale) {
        boolean ru = "ru".equals(locale);
        String subject = ru ? "Запись на занятие отменена — JaiMentorship" : "Сабакка жазуу жокко чыгарылды — JaiMentorship";
        String time = formatSlot(startAt, endAt);
        String body = ru
                ? "Здравствуйте!\n\nЗапись на занятие была отменена.\n\nУчастник: %s\nВремя: %s\n\nС уважением,\nJaiMentorship".formatted(otherPartyName, time)
                : "Саламатсызбы!\n\nСабакка жазуу жокко чыгарылды.\n\nКатышуучу: %s\nУбакыт: %s\n\nУрмат менен,\nJaiMentorship".formatted(otherPartyName, time);
        sendQuietly(toEmail, subject, body, "booking-cancelled");
    }

    @Override
    public void sendBookingCompleted(String toStudentEmail, String mentorName, LocalDateTime startAt, LocalDateTime endAt, String locale) {
        boolean ru = "ru".equals(locale);
        String subject = ru ? "Занятие завершено — JaiMentorship" : "Сабак аяктады — JaiMentorship";
        String time = formatSlot(startAt, endAt);
        String body = ru
                ? "Здравствуйте!\n\nВаше занятие с ментором %s завершено.\nВремя: %s\n\nНе забудьте оставить отзыв!\n\nС уважением,\nJaiMentorship".formatted(mentorName, time)
                : "Саламатсызбы!\n\n%s ментор менен сабагыңыз аяктады.\nУбакыт: %s\n\nПикир калтырууну унутпаңыз!\n\nУрмат менен,\nJaiMentorship".formatted(mentorName, time);
        sendQuietly(toStudentEmail, subject, body, "booking-completed");
    }

    private String formatSlot(LocalDateTime startAt, LocalDateTime endAt) {
        return startAt.format(SLOT_FMT) + " – " + endAt.format(SLOT_FMT);
    }

    /**
     * Send email without propagating exceptions — booking actions must not fail due to mail errors.
     */
    private void sendQuietly(String to, String subject, String body, String eventType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("{} email sent to {}", eventType, to);
        } catch (MailException ex) {
            log.error("Failed to send {} email to {}: {}", eventType, to, ex.getMessage());
        }
    }
}