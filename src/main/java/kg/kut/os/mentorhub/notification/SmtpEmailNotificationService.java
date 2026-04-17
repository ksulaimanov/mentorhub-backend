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

    public SmtpEmailNotificationService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            @Value("${app.mail.from}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromEmail = fromEmail;
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
        java.util.Locale locale = new java.util.Locale(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Подтверждение email в JaiMentorship" : "JaiMentorship'та email тастыктоо";

        Context context = new Context(locale);
        context.setVariable("code", code);

        sendHtmlEmail(toEmail, subject, "mail/confirm-email", context);
    }

    @Async
    @Override
    public void sendPasswordResetCode(String toEmail, String code, String localeStr) {
        java.util.Locale locale = new java.util.Locale(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Сброс пароля в JaiMentorship" : "JaiMentorship'та сырсөздү калыбына келтирүү";

        Context context = new Context(locale);
        context.setVariable("code", code);
        // Using confirm-email for now or could create password-reset if needed
        sendHtmlEmail(toEmail, subject, "mail/confirm-email", context);
    }

    @Async
    @Override
    public void sendApplicationApproved(String toEmail, String userName, String localeStr) {
        java.util.Locale locale = new java.util.Locale(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Ваша заявка на менторство одобрена!" : "Менторлукка арызыңыз кабыл алынды!";

        Context context = new Context(locale);
        context.setVariable("userName", userName);
        context.setVariable("title", subject);
        context.setVariable("message", isRussian ?
            "Вы теперь полноценный ментор на платформе. Можно приступать к заполнению профиля и установке доступных слотов." :
            "Сиз эми платформанын толук укуктуу менторусуз. Профилиңизди толтуруп жана жеткиликтүү убакыттарыңызды белгилей берсеңиз болот.");
        context.setVariable("actionUrl", "https://jaimentorship.com/mentor/profile");

        sendHtmlEmail(toEmail, subject, "mail/new-notification", context);
    }

    @Async
    @Override
    public void sendApplicationRejected(String toEmail, String userName, String rejectionReason, String localeStr) {
        java.util.Locale locale = new java.util.Locale(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Результат рассмотрения заявки на менторство" : "Менторлукка арызды кароонун жыйынтыгы";

        Context context = new Context(locale);
        context.setVariable("userName", userName);
        context.setVariable("title", subject);
        context.setVariable("message", (isRussian ? "К сожалению, ваша заявка была отклонена по причине: " : "Тилекке каршы, сиздин арызыңыз төмөнкү себеп менен четке кагылды: ") + rejectionReason);
        context.setVariable("actionUrl", "https://jaimentorship.com/");

        sendHtmlEmail(toEmail, subject, "mail/new-notification", context);
    }

    @Async
    @Override
    public void sendBookingCreated(String toMentorEmail, String studentName, LocalDateTime startAt, LocalDateTime endAt, String localeStr) {
        java.util.Locale locale = new java.util.Locale(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Новая заявка на занятие!" : "Жаңы сабакка жазылуу!";
        String time = startAt.format(SLOT_FMT);

        Context context = new Context(locale);
        context.setVariable("userName", "Ментор");
        context.setVariable("title", subject);
        context.setVariable("message", (isRussian ? "Студент " : "Студент ") + studentName + (isRussian ? " записался к вам на занятие: " : " сизге сабакка жазылды: ") + time);
        context.setVariable("actionUrl", "https://jaimentorship.com/dashboard/bookings");

        sendHtmlEmail(toMentorEmail, subject, "mail/new-notification", context);
    }

    @Async
    @Override
    public void sendBookingConfirmed(String toStudentEmail, String mentorName, LocalDateTime startAt, LocalDateTime endAt, String localeStr) {
        java.util.Locale locale = new java.util.Locale(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Занятие подтверждено!" : "Сабак тастыкталды!";

        Context context = new Context(locale);
        context.setVariable("studentName", "Студент");
        context.setVariable("mentorName", mentorName);
        context.setVariable("slotTime", startAt.format(SLOT_FMT));

        sendHtmlEmail(toStudentEmail, subject, "mail/booking-approved", context);
    }

    @Async
    @Override
    public void sendBookingCancelled(String toEmail, String otherPartyName, LocalDateTime startAt, LocalDateTime endAt, String localeStr) {
        java.util.Locale locale = new java.util.Locale(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Занятие отменено" : "Сабак жокко чыгарылды";

        Context context = new Context(locale);
        context.setVariable("userName", "Пользователь");
        context.setVariable("title", subject);
        context.setVariable("message", (isRussian ? "Занятие с " : "") + otherPartyName + (isRussian ? " на " : " менен ") + startAt.format(SLOT_FMT) + (isRussian ? " отменено." : " сабагы жокко чыгарылды."));
        context.setVariable("actionUrl", "https://jaimentorship.com/dashboard/bookings");

        sendHtmlEmail(toEmail, subject, "mail/new-notification", context);
    }

    @Async
    @Override
    public void sendBookingCompleted(String toStudentEmail, String mentorName, LocalDateTime startAt, LocalDateTime endAt, String localeStr) {
        java.util.Locale locale = new java.util.Locale(localeStr);
        boolean isRussian = "ru".equals(localeStr);
        String subject = isRussian ? "Урок окончен! Оставьте отзыв" : "Сабак бүттү! Пикир калтырыңыз";

        Context context = new Context(locale);
        context.setVariable("userName", "Студент");
        context.setVariable("title", subject);
        context.setVariable("message", (isRussian ? "Надеемся, вам понравилось занятие с " : "Сизге ") + mentorName + (isRussian ? ". Пожалуйста, оставьте короткий отзыв." : " менен сабак жакты деп үмүттөнөбүз. Сураныч, пикир калтырыңыз."));
        context.setVariable("actionUrl", "https://jaimentorship.com/dashboard/bookings"); // could be link to leave review

        sendHtmlEmail(toStudentEmail, subject, "mail/new-notification", context);
    }
}