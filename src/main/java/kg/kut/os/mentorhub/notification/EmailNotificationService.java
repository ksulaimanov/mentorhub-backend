package kg.kut.os.mentorhub.notification;

public interface EmailNotificationService {
    void sendEmailVerificationCode(String toEmail, String code);
}