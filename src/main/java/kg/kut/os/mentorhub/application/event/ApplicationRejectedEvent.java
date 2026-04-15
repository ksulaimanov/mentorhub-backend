package kg.kut.os.mentorhub.application.event;

import org.springframework.context.ApplicationEvent;

public class ApplicationRejectedEvent extends ApplicationEvent {

    private final String applicantEmail;
    private final String preferredLocale;
    private final String rejectionReason;

    public ApplicationRejectedEvent(Object source, String applicantEmail, String preferredLocale, String rejectionReason) {
        super(source);
        this.applicantEmail = applicantEmail;
        this.preferredLocale = preferredLocale;
        this.rejectionReason = rejectionReason;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public String getPreferredLocale() {
        return preferredLocale;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
