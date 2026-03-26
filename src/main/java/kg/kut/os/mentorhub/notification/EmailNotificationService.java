package kg.kut.os.mentorhub.notification;

public interface EmailNotificationService {
    void sendEmailVerificationCode(String toEmail, String code);
    void sendPasswordResetCode(String toEmail, String code);
    void sendApplicationApproved(String toEmail, String userName);
    void sendApplicationRejected(String toEmail, String userName, String rejectionReason);
}