package kg.kut.os.mentorhub.booking.service;

import kg.kut.os.mentorhub.availability.entity.MentorAvailabilitySlot;
import kg.kut.os.mentorhub.availability.repository.MentorAvailabilitySlotRepository;
import kg.kut.os.mentorhub.booking.dto.BookingResponse;
import kg.kut.os.mentorhub.booking.dto.CreateBookingRequest;
import kg.kut.os.mentorhub.booking.dto.UpdateBookingStatusRequest;
import kg.kut.os.mentorhub.booking.entity.Booking;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import kg.kut.os.mentorhub.booking.repository.BookingRepository;
import kg.kut.os.mentorhub.common.exception.BadRequestException;
import kg.kut.os.mentorhub.common.exception.NotFoundException;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import kg.kut.os.mentorhub.notification.entity.NotificationType;
import kg.kut.os.mentorhub.notification.EmailNotificationService;
import kg.kut.os.mentorhub.notification.service.InAppNotificationService;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    /**
     * Strict state machine: from-status → allowed to-statuses.
     *
     * PENDING    → CONFIRMED, CANCELLED_BY_STUDENT, CANCELLED_BY_MENTOR
     * CONFIRMED  → COMPLETED, CANCELLED_BY_STUDENT, CANCELLED_BY_MENTOR
     * COMPLETED  → (terminal)
     * CANCELLED_BY_STUDENT → (terminal)
     * CANCELLED_BY_MENTOR  → (terminal)
     */
    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = Map.of(
            BookingStatus.PENDING, Set.of(
                    BookingStatus.CONFIRMED,
                    BookingStatus.CANCELLED_BY_STUDENT,
                    BookingStatus.CANCELLED_BY_MENTOR
            ),
            BookingStatus.CONFIRMED, Set.of(
                    BookingStatus.COMPLETED,
                    BookingStatus.CANCELLED_BY_STUDENT,
                    BookingStatus.CANCELLED_BY_MENTOR
            ),
            BookingStatus.COMPLETED, Set.of(),
            BookingStatus.CANCELLED_BY_STUDENT, Set.of(),
            BookingStatus.CANCELLED_BY_MENTOR, Set.of()
    );

    private final BookingRepository bookingRepository;
    private final MentorAvailabilitySlotRepository availabilitySlotRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final StorageService storageService;
    private final EmailNotificationService emailNotificationService;
    private final InAppNotificationService inAppNotificationService;

    public BookingService(
            BookingRepository bookingRepository,
            MentorAvailabilitySlotRepository availabilitySlotRepository,
            StudentProfileRepository studentProfileRepository,
            MentorProfileRepository mentorProfileRepository,
            StorageService storageService,
            EmailNotificationService emailNotificationService,
            InAppNotificationService inAppNotificationService
    ) {
        this.bookingRepository = bookingRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.storageService = storageService;
        this.emailNotificationService = emailNotificationService;
        this.inAppNotificationService = inAppNotificationService;
    }

    // ----------------------------------------------------------------
    // Create
    // ----------------------------------------------------------------

    public BookingResponse createBooking(Long studentUserId, CreateBookingRequest request) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new NotFoundException("Профиль ученика не найден"));

        MentorAvailabilitySlot slot = availabilitySlotRepository.findByIdForUpdate(request.getAvailabilitySlotId())
                .orElseThrow(() -> new NotFoundException("Слот не найден"));

        if (!slot.isActive()) {
            throw new BadRequestException("Слот недоступен для записи");
        }

        if (!slot.getStartAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Нельзя записаться на прошедший слот");
        }

        // Prevent booking your own slot
        MentorProfile mentor = slot.getMentor();
        if (mentor.getUser().getId().equals(studentUserId)) {
            throw new BadRequestException("Нельзя записаться на собственный слот");
        }

        // Prevent duplicate active booking for the same slot
        boolean alreadyBooked = bookingRepository.existsByStudentIdAndAvailabilitySlotIdAndStatusIn(
                student.getId(), slot.getId(), ACTIVE_BOOKING_STATUSES
        );
        if (alreadyBooked) {
            throw new BadRequestException("Вы уже записаны на этот слот");
        }

        long bookedCount = bookingRepository.countByAvailabilitySlotIdAndStatusIn(
                slot.getId(),
                ACTIVE_BOOKING_STATUSES
        );

        if (bookedCount >= slot.getCapacity()) {
            throw new BadRequestException("Свободных мест в этом слоте больше нет");
        }

        Booking booking = new Booking();
        booking.setStudent(student);
        booking.setMentor(mentor);
        booking.setAvailabilitySlot(slot);
        booking.setStartAt(slot.getStartAt());
        booking.setEndAt(slot.getEndAt());
        booking.setTimezone(slot.getTimezone());
        booking.setLessonFormat(slot.getLessonFormat());
        booking.setMeetingLink(slot.getMeetingLink());
        booking.setAddressText(slot.getAddressText());
        booking.setStatus(BookingStatus.PENDING);
        booking.setStudentNote(request.getStudentNote());

        Booking savedBooking = bookingRepository.save(booking);

        notifyBookingCreated(savedBooking);

        return map(savedBooking);
    }

    // ----------------------------------------------------------------
    // Student actions
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BookingResponse> getStudentBookings(Long studentUserId, BookingStatus status) {
        List<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findAllByStudentUserIdAndStatusFetched(studentUserId, status);
        } else {
            bookings = bookingRepository.findAllByStudentUserIdFetched(studentUserId);
        }
        return bookings.stream().map(this::map).toList();
    }

    public BookingResponse cancelByStudent(Long studentUserId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndStudentUserIdFetched(bookingId, studentUserId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        assertTransition(booking, BookingStatus.CANCELLED_BY_STUDENT);

        booking.setStatus(BookingStatus.CANCELLED_BY_STUDENT);

        notifyBookingCancelledByStudent(booking);

        return map(booking);
    }

    // ----------------------------------------------------------------
    // Mentor actions
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BookingResponse> getMentorBookings(Long mentorUserId, BookingStatus status) {
        List<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findAllByMentorUserIdAndStatusFetched(mentorUserId, status);
        } else {
            bookings = bookingRepository.findAllByMentorUserIdFetched(mentorUserId);
        }
        return bookings.stream().map(this::map).toList();
    }

    public BookingResponse confirmByMentor(Long mentorUserId, Long bookingId, String mentorNote) {
        Booking booking = bookingRepository.findByIdAndMentorUserIdFetched(bookingId, mentorUserId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        assertTransition(booking, BookingStatus.CONFIRMED);

        booking.setStatus(BookingStatus.CONFIRMED);
        if (mentorNote != null) {
            booking.setMentorNote(mentorNote);
        }

        notifyBookingConfirmed(booking);

        return map(booking);
    }

    public BookingResponse declineByMentor(Long mentorUserId, Long bookingId, String mentorNote) {
        Booking booking = bookingRepository.findByIdAndMentorUserIdFetched(bookingId, mentorUserId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        assertTransition(booking, BookingStatus.CANCELLED_BY_MENTOR);

        booking.setStatus(BookingStatus.CANCELLED_BY_MENTOR);
        if (mentorNote != null) {
            booking.setMentorNote(mentorNote);
        }

        notifyBookingDeclinedByMentor(booking);

        return map(booking);
    }

    public BookingResponse completeByMentor(Long mentorUserId, Long bookingId, String mentorNote) {
        Booking booking = bookingRepository.findByIdAndMentorUserIdFetched(bookingId, mentorUserId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        assertTransition(booking, BookingStatus.COMPLETED);

        booking.setStatus(BookingStatus.COMPLETED);
        if (mentorNote != null) {
            booking.setMentorNote(mentorNote);
        }

        notifyBookingCompleted(booking);

        return map(booking);
    }

    /**
     * Generic status update — kept for backward compatibility.
     * Delegates to the specific action methods.
     */
    public BookingResponse updateMentorBookingStatus(Long mentorUserId, Long bookingId, UpdateBookingStatusRequest request) {
        return switch (request.getStatus()) {
            case CONFIRMED -> confirmByMentor(mentorUserId, bookingId, request.getMentorNote());
            case CANCELLED_BY_MENTOR -> declineByMentor(mentorUserId, bookingId, request.getMentorNote());
            case COMPLETED -> completeByMentor(mentorUserId, bookingId, request.getMentorNote());
            default -> throw new BadRequestException(
                    "Ментор может установить только статусы: CONFIRMED, CANCELLED_BY_MENTOR, COMPLETED"
            );
        };
    }

    // ----------------------------------------------------------------
    // Transition enforcement
    // ----------------------------------------------------------------

    private void assertTransition(Booking booking, BookingStatus target) {
        Set<BookingStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(booking.getStatus(), Set.of());
        if (!allowed.contains(target)) {
            throw new BadRequestException(
                    "Невозможно перевести бронирование из статуса " + booking.getStatus() + " в " + target
            );
        }
    }

    // ----------------------------------------------------------------
    // Email notifications (fire-and-forget, errors are logged but not propagated)
    // ----------------------------------------------------------------

    private void notifyBookingCreated(Booking booking) {
        try {
            String mentorEmail = booking.getMentor().getUser().getEmail();
            String mentorLocale = booking.getMentor().getUser().getPreferredLocale();
            String studentName = safeName(booking.getStudent().getFirstName(), booking.getStudent().getLastName());
            emailNotificationService.sendBookingCreated(
                    mentorEmail, studentName,
                    booking.getStartAt(), booking.getEndAt(), mentorLocale
            );
        } catch (Exception ex) {
            log.warn("Failed to send booking-created notification for booking {}: {}", booking.getId(), ex.getMessage());
        }
    }

    private void notifyBookingConfirmed(Booking booking) {
        try {
            String studentEmail = booking.getStudent().getUser().getEmail();
            String studentLocale = booking.getStudent().getUser().getPreferredLocale();
            String mentorName = safeName(booking.getMentor().getFirstName(), booking.getMentor().getLastName());
            emailNotificationService.sendBookingConfirmed(
                    studentEmail, mentorName,
                    booking.getStartAt(), booking.getEndAt(), studentLocale
            );
        } catch (Exception ex) {
            log.warn("Failed to send booking-confirmed notification for booking {}: {}", booking.getId(), ex.getMessage());
        }
    }

    private void notifyBookingCancelledByStudent(Booking booking) {
        try {
            String mentorEmail = booking.getMentor().getUser().getEmail();
            String mentorLocale = booking.getMentor().getUser().getPreferredLocale();
            String studentName = safeName(booking.getStudent().getFirstName(), booking.getStudent().getLastName());
            emailNotificationService.sendBookingCancelled(
                    mentorEmail, studentName,
                    booking.getStartAt(), booking.getEndAt(), mentorLocale
            );
        } catch (Exception ex) {
            log.warn("Failed to send booking-cancelled notification for booking {}: {}", booking.getId(), ex.getMessage());
        }
    }

    private void notifyBookingDeclinedByMentor(Booking booking) {
        try {
            String studentEmail = booking.getStudent().getUser().getEmail();
            String studentLocale = booking.getStudent().getUser().getPreferredLocale();
            String mentorName = safeName(booking.getMentor().getFirstName(), booking.getMentor().getLastName());
            emailNotificationService.sendBookingCancelled(
                    studentEmail, mentorName,
                    booking.getStartAt(), booking.getEndAt(), studentLocale
            );
        } catch (Exception ex) {
            log.warn("Failed to send booking-declined notification for booking {}: {}", booking.getId(), ex.getMessage());
        }
    }

    private void notifyBookingCompleted(Booking booking) {
        try {
            String studentEmail = booking.getStudent().getUser().getEmail();
            String studentLocale = booking.getStudent().getUser().getPreferredLocale();
            String mentorName = safeName(booking.getMentor().getFirstName(), booking.getMentor().getLastName());
            emailNotificationService.sendBookingCompleted(
                    studentEmail, mentorName,
                    booking.getStartAt(), booking.getEndAt(), studentLocale
            );

            // IN-APP NOTIFICATION FOR REVIEW
            inAppNotificationService.createNotification(
                    booking.getStudent().getUser().getId(),
                    NotificationType.LESSON_COMPLETED,
                    "Урок окончен!",
                    "Оставьте отзыв о менторе " + mentorName + ", чтобы помочь другим",
                    "/mentor/" + booking.getMentor().getId() + "?leaveReview=" + booking.getId()
            );

        } catch (Exception ex) {
            log.warn("Failed to send booking-completed notification for booking {}: {}", booking.getId(), ex.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Mapping
    // ----------------------------------------------------------------

    private BookingResponse map(Booking booking) {
        MentorProfile mentor = booking.getMentor();
        StudentProfile student = booking.getStudent();

        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setStudentId(student.getId());
        response.setMentorId(mentor.getId());
        response.setAvailabilitySlotId(booking.getAvailabilitySlot().getId());
        response.setStartAt(booking.getStartAt());
        response.setEndAt(booking.getEndAt());
        response.setTimezone(booking.getTimezone());
        response.setLessonFormat(booking.getLessonFormat());
        response.setMeetingLink(booking.getMeetingLink());
        response.setAddressText(booking.getAddressText());
        response.setStatus(booking.getStatus());
        response.setStudentNote(booking.getStudentNote());
        response.setMentorNote(booking.getMentorNote());

        response.setMentorFirstName(mentor.getFirstName());
        response.setMentorLastName(mentor.getLastName());
        response.setMentorAvatarUrl(storageService.buildPublicUrl(mentor.getAvatarKey()));
        response.setStudentFirstName(student.getFirstName());
        response.setStudentLastName(student.getLastName());
        response.setStudentAvatarUrl(storageService.buildPublicUrl(student.getAvatarKey()));
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());

        return response;
    }

    private String safeName(String firstName, String lastName) {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "Пользователь" : full;
    }
}