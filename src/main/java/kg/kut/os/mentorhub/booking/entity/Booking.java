package kg.kut.os.mentorhub.booking.entity;

import jakarta.persistence.*;
import kg.kut.os.mentorhub.availability.entity.LessonFormat;
import kg.kut.os.mentorhub.availability.entity.MentorAvailabilitySlot;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.student.entity.StudentProfile;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private MentorProfile mentor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_slot_id", nullable = false)
    private MentorAvailabilitySlot availabilitySlot;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false, length = 100)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_format", nullable = false, length = 20)
    private LessonFormat lessonFormat;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "address_text", length = 255)
    private String addressText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status;

    @Column(name = "student_note", length = 1000)
    private String studentNote;

    @Column(name = "mentor_note", length = 1000)
    private String mentorNote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = BookingStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Booking() {
    }

    public Long getId() {
        return id;
    }

    public StudentProfile getStudent() {
        return student;
    }

    public MentorProfile getMentor() {
        return mentor;
    }

    public MentorAvailabilitySlot getAvailabilitySlot() {
        return availabilitySlot;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public String getTimezone() {
        return timezone;
    }

    public LessonFormat getLessonFormat() {
        return lessonFormat;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public String getAddressText() {
        return addressText;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getStudentNote() {
        return studentNote;
    }

    public String getMentorNote() {
        return mentorNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStudent(StudentProfile student) {
        this.student = student;
    }

    public void setMentor(MentorProfile mentor) {
        this.mentor = mentor;
    }

    public void setAvailabilitySlot(MentorAvailabilitySlot availabilitySlot) {
        this.availabilitySlot = availabilitySlot;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setLessonFormat(LessonFormat lessonFormat) {
        this.lessonFormat = lessonFormat;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setStudentNote(String studentNote) {
        this.studentNote = studentNote;
    }

    public void setMentorNote(String mentorNote) {
        this.mentorNote = mentorNote;
    }
}