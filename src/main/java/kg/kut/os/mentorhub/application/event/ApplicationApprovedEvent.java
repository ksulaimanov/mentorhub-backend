package kg.kut.os.mentorhub.application.event;

import org.springframework.context.ApplicationEvent;

public class ApplicationApprovedEvent extends ApplicationEvent {

    private final String applicantEmail;
    private final String preferredLocale;

    public ApplicationApprovedEvent(Object source, String applicantEmail, String preferredLocale) {
        super(source);
        this.applicantEmail = applicantEmail;
        this.preferredLocale = preferredLocale;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public String getPreferredLocale() {
        return preferredLocale;
    }
}
