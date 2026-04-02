package kg.kut.os.mentorhub.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    @Override
    public void sendBookingCreated(String toMentorEmail, String studentName, LocalDateTime startAt, LocalDateTime endAt, String locale) {
        log.info("MAIL DISABLED. Booking created: to={}, student={}, slot={} - {} (locale={})", toMentorEmail, studentName, startAt, endAt, locale);
    }

    @Override
    public void sendBookingConfirmed(String toStudentEmail, String mentorName, LocalDateTime startAt, LocalDateTime endAt, String locale) {
        log.info("MAIL DISABLED. Booking confirmed: to={}, mentor={}, slot={} - {} (locale={})", toStudentEmail, mentorName, startAt, endAt, locale);
    }

    @Override
    public void sendBookingCancelled(String toEmail, String otherPartyName, LocalDateTime startAt, LocalDateTime endAt, String locale) {
        log.info("MAIL DISABLED. Booking cancelled: to={}, by={}, slot={} - {} (locale={})", toEmail, otherPartyName, startAt, endAt, locale);
    }

    @Override
    public void sendBookingCompleted(String toStudentEmail, String mentorName, LocalDateTime startAt, LocalDateTime endAt, String locale) {
        log.info("MAIL DISABLED. Booking completed: to={}, mentor={}, slot={} - {} (locale={})", toStudentEmail, mentorName, startAt, endAt, locale);
    }
}