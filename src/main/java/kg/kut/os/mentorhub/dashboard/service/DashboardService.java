package kg.kut.os.mentorhub.dashboard.service;

import kg.kut.os.mentorhub.auth.entity.RoleCode;
import kg.kut.os.mentorhub.auth.repository.UserRepository;
import kg.kut.os.mentorhub.booking.entity.Booking;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import kg.kut.os.mentorhub.booking.repository.BookingRepository;
import kg.kut.os.mentorhub.dashboard.dto.AdminDashboardDto;
import kg.kut.os.mentorhub.dashboard.dto.DashboardStatsDto;
import kg.kut.os.mentorhub.dashboard.dto.MentorDashboardDto;
import kg.kut.os.mentorhub.dashboard.dto.StudentDashboardDto;
import kg.kut.os.mentorhub.dashboard.dto.UpcomingEventDto;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import kg.kut.os.mentorhub.review.repository.ReviewRepository;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int UPCOMING_LIMIT = 5;
    private static final int ADMIN_RECENT_LIMIT = 10;

    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final ReviewRepository reviewRepository;
    private final StorageService storageService;

    public DashboardService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            MentorProfileRepository mentorProfileRepository,
            ReviewRepository reviewRepository,
            StorageService storageService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.reviewRepository = reviewRepository;
        this.storageService = storageService;
    }

    // ----------------------------------------------------------------
    // Student
    // ----------------------------------------------------------------

    public StudentDashboardDto getStudentDashboard(Long studentUserId) {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> upcoming = bookingRepository.findUpcomingByStudentUserId(
                studentUserId, ACTIVE_STATUSES, now, PageRequest.of(0, UPCOMING_LIMIT));

        long totalBookings = bookingRepository.countByStudentUserId(studentUserId);
        long completedBookings = bookingRepository.countByStudentUserIdAndStatus(
                studentUserId, BookingStatus.COMPLETED);

        // batch-load bookedCount per slot
        Map<Long, Long> bookedCountBySlot = batchBookedCounts(upcoming);

        StudentDashboardDto dto = new StudentDashboardDto();
        dto.setUpcomingEvents(upcoming.stream()
                .map(b -> toUpcomingEventDto(b, bookedCountBySlot))
                .toList());
        dto.setTotalBookings(totalBookings);
        dto.setCompletedBookings(completedBookings);
        return dto;
    }

    // ----------------------------------------------------------------
    // Mentor
    // ----------------------------------------------------------------

    public MentorDashboardDto getMentorDashboard(Long mentorUserId) {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> upcoming = bookingRepository.findUpcomingByMentorUserId(
                mentorUserId, ACTIVE_STATUSES, now, PageRequest.of(0, UPCOMING_LIMIT));

        long totalBookings = bookingRepository.countByMentorUserId(mentorUserId);
        long completedBookings = bookingRepository.countByMentorUserIdAndStatus(
                mentorUserId, BookingStatus.COMPLETED);

        // distinct students who ever booked this mentor (ACTIVE or COMPLETED)
        long totalStudents = bookingRepository.countDistinctStudentsByMentorUserId(mentorUserId);

        // average rating from reviews
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(mentorUserId)
                .orElse(null);
        double averageRating = 0.0;
        if (mentorProfile != null) {
            BigDecimal avg = reviewRepository.findAverageRatingByMentorId(mentorProfile.getId());
            averageRating = avg != null ? avg.doubleValue() : 0.0;
        }

        Map<Long, Long> bookedCountBySlot = batchBookedCounts(upcoming);

        MentorDashboardDto dto = new MentorDashboardDto();
        dto.setUpcomingEvents(upcoming.stream()
                .map(b -> toUpcomingEventDto(b, bookedCountBySlot))
                .toList());
        dto.setTotalBookings(totalBookings);
        dto.setCompletedBookings(completedBookings);
        dto.setTotalStudents(totalStudents);
        dto.setAverageRating(averageRating);
        return dto;
    }

    // ----------------------------------------------------------------
    // Admin
    // ----------------------------------------------------------------

    public AdminDashboardDto getAdminDashboard() {
        LocalDateTime now = LocalDateTime.now();

        long totalMentors  = userRepository.countByRoleCode(RoleCode.ROLE_MENTOR);
        long totalStudents = userRepository.countByRoleCode(RoleCode.ROLE_STUDENT);
        long totalBookings = bookingRepository.count();
        long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long upcomingBookings  = bookingRepository.countUpcoming(ACTIVE_STATUSES, now);

        // global average rating across all mentors
        BigDecimal globalAvg = reviewRepository.findGlobalAverageRating();
        double averageRating = globalAvg != null ? globalAvg.doubleValue() : 0.0;

        List<Booking> recent = bookingRepository.findTopUpcomingForAdmin(
                ACTIVE_STATUSES, now, PageRequest.of(0, ADMIN_RECENT_LIMIT));

        Map<Long, Long> bookedCountBySlot = batchBookedCounts(recent);

        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalBookings(totalBookings);
        stats.setCompletedBookings(completedBookings);
        stats.setUpcomingBookings(upcomingBookings);
        stats.setTotalStudents(totalStudents);
        stats.setTotalMentors(totalMentors);
        stats.setAverageRating(averageRating);

        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setStats(stats);
        dto.setRecentEvents(recent.stream()
                .map(b -> toUpcomingEventDto(b, bookedCountBySlot))
                .toList());
        return dto;
    }

    // ----------------------------------------------------------------
    // Single unified mapper → UpcomingEventDto
    // ----------------------------------------------------------------

    private UpcomingEventDto toUpcomingEventDto(Booking b, Map<Long, Long> bookedCountBySlot) {
        MentorProfile mentor   = b.getMentor();
        StudentProfile student = b.getStudent();

        long bookedCount = bookedCountBySlot.getOrDefault(b.getAvailabilitySlot().getId(), 0L);
        int capacity     = b.getAvailabilitySlot().getCapacity();

        String mentorName = trim(mentor.getFirstName()) + " " + trim(mentor.getLastName());
        String studentName = trim(student.getFirstName()) + " " + trim(student.getLastName());

        // title: "Занятие с <mentorFirstName>" for student side, full name always populated
        String title = "Занятие с " + trim(mentor.getFirstName());

        UpcomingEventDto dto = new UpcomingEventDto();
        dto.setId(b.getId());
        dto.setType("BOOKING");
        dto.setTitle(title);
        dto.setDescription(b.getStudentNote());
        dto.setStartAt(b.getStartAt().atOffset(ZoneOffset.UTC));
        dto.setEndAt(b.getEndAt().atOffset(ZoneOffset.UTC));
        dto.setMentorName(mentorName.isBlank() ? null : mentorName.trim());
        dto.setStudentName(studentName.isBlank() ? null : studentName.trim());
        dto.setLessonFormat(b.getLessonFormat().name());
        dto.setStatus(b.getStatus().name());
        dto.setCapacity(capacity);
        dto.setBookedCount((int) bookedCount);
        dto.setAvailableSeats(Math.max(capacity - (int) bookedCount, 0));
        return dto;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    /** Batch-loads bookedCount per distinct slotId from already-fetched bookings. */
    private Map<Long, Long> batchBookedCounts(List<Booking> bookings) {
        List<Long> slotIds = bookings.stream()
                .map(b -> b.getAvailabilitySlot().getId())
                .distinct()
                .toList();
        return slotIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> bookingRepository.countByAvailabilitySlotIdAndStatusIn(id, ACTIVE_STATUSES)
                ));
    }

    private String trim(String s) {
        return s != null ? s.trim() : "";
    }
}
