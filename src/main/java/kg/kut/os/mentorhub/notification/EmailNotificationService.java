package kg.kut.os.mentorhub.notification;

import java.time.LocalDateTime;

public interface EmailNotificationService {
    void sendEmailVerificationCode(String toEmail, String code, String locale);
    void sendPasswordResetCode(String toEmail, String code, String locale);
    void sendApplicationApproved(String toEmail, String userName, String locale);
    void sendApplicationRejected(String toEmail, String userName, String rejectionReason, String locale);

    // Booking lifecycle notifications
    void sendBookingCreated(String toMentorEmail, String studentName, LocalDateTime startAt, LocalDateTime endAt, String locale);
    void sendBookingConfirmed(String toStudentEmail, String mentorName, LocalDateTime startAt, LocalDateTime endAt, String locale);
    void sendBookingCancelled(String toEmail, String otherPartyName, LocalDateTime startAt, LocalDateTime endAt, String locale);
    void sendBookingCompleted(String toStudentEmail, String mentorName, LocalDateTime startAt, LocalDateTime endAt, String locale);
}