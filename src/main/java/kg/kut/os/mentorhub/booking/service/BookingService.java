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
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BookingService {

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private final BookingRepository bookingRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final MentorAvailabilitySlotRepository slotRepository;
    private final StorageService storageService;

    public BookingService(
            BookingRepository bookingRepository,
            StudentProfileRepository studentProfileRepository,
            MentorAvailabilitySlotRepository slotRepository,
            StorageService storageService
    ) {
        this.bookingRepository = bookingRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.slotRepository = slotRepository;
        this.storageService = storageService;
    }

    public BookingResponse createBooking(Long studentUserId, CreateBookingRequest request) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new NotFoundException("Профиль ученика не найден"));

        MentorAvailabilitySlot slot = slotRepository.findByIdForUpdate(request.getAvailabilitySlotId())
                .orElseThrow(() -> new NotFoundException("Слот не найден"));

        if (!slot.isActive()) {
            throw new BadRequestException("Слот недоступен для записи");
        }

        if (!slot.getStartAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Нельзя записаться на прошедший слот");
        }

        long bookedCount = bookingRepository.countByAvailabilitySlotIdAndStatusIn(
                slot.getId(),
                ACTIVE_BOOKING_STATUSES
        );

        if (bookedCount >= slot.getCapacity()) {
            throw new BadRequestException("Свободных мест в этом слоте больше нет");
        }

        MentorProfile mentor = slot.getMentor();

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

        return map(savedBooking);
    }

    public List<BookingResponse> getStudentBookings(Long studentUserId, BookingStatus status) {
        List<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findAllByStudentUserIdAndStatusFetched(studentUserId, status);
        } else {
            bookings = bookingRepository.findAllByStudentUserIdFetched(studentUserId);
        }
        return bookings.stream().map(this::map).toList();
    }

    public List<BookingResponse> getMentorBookings(Long mentorUserId, BookingStatus status) {
        List<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findAllByMentorUserIdAndStatusFetched(mentorUserId, status);
        } else {
            bookings = bookingRepository.findAllByMentorUserIdFetched(mentorUserId);
        }
        return bookings.stream().map(this::map).toList();
    }

    public void cancelByStudent(Long studentUserId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndStudentUserId(bookingId, studentUserId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (booking.getStatus() == BookingStatus.CANCELLED_BY_STUDENT ||
                booking.getStatus() == BookingStatus.CANCELLED_BY_MENTOR ||
                booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Это бронирование уже нельзя отменить");
        }

        booking.setStatus(BookingStatus.CANCELLED_BY_STUDENT);
    }

    public BookingResponse updateMentorBookingStatus(Long mentorUserId, Long bookingId, UpdateBookingStatusRequest request) {
        Booking booking = bookingRepository.findByIdAndMentorUserId(bookingId, mentorUserId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (request.getStatus() != BookingStatus.CONFIRMED &&
                request.getStatus() != BookingStatus.CANCELLED_BY_MENTOR &&
                request.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Ментор может установить только статусы CONFIRMED, CANCELLED_BY_MENTOR, COMPLETED");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED_BY_STUDENT ||
                booking.getStatus() == BookingStatus.CANCELLED_BY_MENTOR) {
            throw new BadRequestException("Отменённое бронирование нельзя изменить");
        }

        booking.setStatus(request.getStatus());
        booking.setMentorNote(request.getMentorNote());

        return map(booking);
    }

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
}