package kg.kut.os.mentorhub.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true", matchIfMissing = true)
public class SmtpEmailNotificationService implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailNotificationService.class);
    private static final DateTimeFormatter SLOT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String fromEmail;
    private final String frontendUrl;

    public SmtpEmailNotificationService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            @Value("${app.mail.from}") String fromEmail,
            @Value("${app.frontend.url:https://jaimentorship.kutman.me}") String frontendUrl
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromEmail = fromEmail;
        this.frontendUrl = frontendUrl;
    }

    private void sendHtmlEmail(String toEmail, String subject, String templateName, Context context) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("HTML email sent to {}", toEmail);
        } catch (MessagingException | MailException ex) {
            log.error("Failed to send HTML email to {}", toEmail, ex);
        }
    }

    @Async
    @Override
    public void sendEmailVerificationCode(String toEmail, String code, String localeStr) {
        java.util.Locale locale = java.util.Locale.forLanguageTag(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Подтверждение email в JaiMentorship" : "JaiMentorship'та email тастыктоо";

        Context context = new Context(locale);
        context.setVariable("code", code);

        sendHtmlEmail(toEmail, subject, "mail/confirm-email", context);
    }

    @Async
    @Override
    public void sendPasswordResetCode(String toEmail, String userName, String code, String localeStr) {
        java.util.Locale locale = java.util.Locale.forLanguageTag(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Сброс пароля в JaiMentorship" : "JaiMentorship'та сырсөздү калыбына келтирүү";
        Context context = new Context(locale);
        context.setVariable("userName", userName != null ? userName : (isRussian ? "Пользователь" : "Колдонуучу"));
        context.setVariable("title", subject);

        String resetUrl = frontendUrl + "/auth/reset-password?email=" + toEmail + "&code=" + code;

        context.setVariable("message", isRussian ?
                "Вы запросили сброс пароля. Ваш код для сброса: " + code + ". Вы также можете нажать на кнопку ниже для сброса:" :
                "Сиз сырсөздү калыбына келтирүүнү сурадыңыз. Сиздин код: " + code + ". Же төмөнкү баскычты басыңыз:");
        context.setVariable("actionUrl", resetUrl);

        sendHtmlEmail(toEmail, subject, "mail/password-reset", context);
    }

    @Async
    @Override
    public void sendApplicationApproved(String toEmail, String userName, String localeStr) {
        java.util.Locale locale = java.util.Locale.forLanguageTag(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Ваша заявка на менторство одобрена!" : "Менторлукка арызыңыз кабыл алынды!";

        Context context = new Context(locale);
        context.setVariable("userName", userName);
        context.setVariable("title", subject);
        context.setVariable("message", isRussian ?
            "Вы теперь полноценный ментор на платформе. Можно приступать к заполнению профиля и установке доступных слотов." :
            "Сиз эми платформанын толук укуктуу менторусуз. Профилиңизди толтуруп жана жеткиликтүү убакыттарыңызды белгилей берсеңиз болот.");
        context.setVariable("actionUrl", frontendUrl + "/mentor/profile");

        sendHtmlEmail(toEmail, subject, "mail/application-approved", context);
    }

    @Async
    @Override
    public void sendApplicationRejected(String toEmail, String userName, String rejectionReason, String localeStr) {
        java.util.Locale locale = java.util.Locale.forLanguageTag(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Результат рассмотрения заявки на менторство" : "Менторлукка арызды кароонун жыйынтыгы";

        Context context = new Context(locale);
        context.setVariable("userName", userName);
        context.setVariable("title", subject);
        context.setVariable("message", (isRussian ? "К сожалению, ваша заявка была отклонена по причине: " : "Тилекке каршы, сиздин арызыңыз төмөнкү себеп менен четке кагылды: ") + rejectionReason);
        context.setVariable("actionUrl", frontendUrl + "/");

        sendHtmlEmail(toEmail, subject, "mail/application-rejected", context);
    }

    @Async
    @Override
    public void sendBookingCreated(String toMentorEmail, String studentName, LocalDateTime startAt, LocalDateTime endAt, String localeStr) {
        java.util.Locale locale = java.util.Locale.forLanguageTag(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Новая заявка на занятие!" : "Жаңы сабакка жазылуу!";
        String time = startAt.format(SLOT_FMT);

        Context context = new Context(locale);
        context.setVariable("userName", "Ментор");
        context.setVariable("title", subject);
        context.setVariable("message", "Студент " + studentName + (isRussian ? " записался к вам на занятие: " : " сизге сабакка жазылды: ") + time);
        context.setVariable("actionUrl", frontendUrl + "/dashboard/bookings");

        sendHtmlEmail(toMentorEmail, subject, "mail/booking-created", context);
    }

    @Async
    @Override
    public void sendBookingConfirmed(String toStudentEmail, String mentorName, LocalDateTime startAt, LocalDateTime endAt, String localeStr) {
        java.util.Locale locale = java.util.Locale.forLanguageTag(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Занятие подтверждено!" : "Сабак тастыкталды!";

        Context context = new Context(locale);
        context.setVariable("studentName", "Студент");
        context.setVariable("mentorName", mentorName);
        context.setVariable("slotTime", startAt.format(SLOT_FMT));
        context.setVariable("actionUrl", frontendUrl + "/dashboard/bookings");

        sendHtmlEmail(toStudentEmail, subject, "mail/booking-approved", context);
    }

    @Async
    @Override
    public void sendBookingCancelled(String toEmail, String otherPartyName, LocalDateTime startAt, LocalDateTime endAt, String localeStr) {
        java.util.Locale locale = java.util.Locale.forLanguageTag(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Занятие отменено" : "Сабак жокко чыгарылды";

        Context context = new Context(locale);
        context.setVariable("userName", "Пользователь");
        context.setVariable("title", subject);
        context.setVariable("message", (isRussian ? "Занятие с " : "") + otherPartyName + (isRussian ? " на " : " менен ") + startAt.format(SLOT_FMT) + (isRussian ? " отменено." : " сабагы жокко чыгарылды."));
        context.setVariable("actionUrl", frontendUrl + "/dashboard/bookings");

        sendHtmlEmail(toEmail, subject, "mail/lesson-cancelled", context);
    }

    @Async
    @Override
    public void sendBookingCompleted(String toStudentEmail, String mentorName, LocalDateTime startAt, LocalDateTime endAt, String localeStr) {
        java.util.Locale locale = java.util.Locale.forLanguageTag(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Урок окончен! Оставьте отзыв" : "Сабак бүттү! Пикир калтырыңыз";

        Context context = new Context(locale);
        context.setVariable("userName", "Студент");
        context.setVariable("title", subject);
        context.setVariable("message", (isRussian ? "Надеемся, вам понравилось занятие с " : "Сизге ") + mentorName + (isRussian ? ". Пожалуйста, оставьте короткий отзыв." : " менен сабак жакты деп үмүттөнөбүз. Сураныч, пикир калтырыңыз."));
        context.setVariable("actionUrl", frontendUrl + "/dashboard/bookings");

        sendHtmlEmail(toStudentEmail, subject, "mail/lesson-completed", context);
    }
}