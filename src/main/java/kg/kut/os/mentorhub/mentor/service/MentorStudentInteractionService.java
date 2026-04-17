package kg.kut.os.mentorhub.mentor.service;

import kg.kut.os.mentorhub.booking.entity.Booking;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import kg.kut.os.mentorhub.booking.repository.BookingRepository;
import kg.kut.os.mentorhub.common.exception.NotFoundException;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.mentor.dto.BookingHistoryDto;
import kg.kut.os.mentorhub.mentor.dto.StudentPreviewDto;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class MentorStudentInteractionService {

    private final BookingRepository bookingRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StorageService storageService;

    public MentorStudentInteractionService(
            BookingRepository bookingRepository,
            StudentProfileRepository studentProfileRepository,
            StorageService storageService
    ) {
        this.bookingRepository = bookingRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.storageService = storageService;
    }

    public StudentPreviewDto getStudentPreview(Long mentorUserId, Long studentUserId) {
        // 1. Check access: Mentor can only see this specific preview if there's an interaction (booking)
        boolean hasInteracted = bookingRepository.existsByStudentUserIdAndMentorUserId(studentUserId, mentorUserId);
        if (!hasInteracted) {
            throw new AccessDeniedException("У вас нет доступа к данным этого студента, так как между вами не было занятий.");
        }

        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new NotFoundException("Студент не найден"));

        StudentPreviewDto dto = new StudentPreviewDto();
        dto.setStudentUserId(studentUserId);
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());

        String displayName = (safeString(student.getFirstName()) + " " + safeString(student.getLastName())).trim();
        dto.setDisplayName(displayName.isEmpty() ? student.getUser().getEmail() : displayName);

        dto.setAvatarUrl(storageService.buildPublicUrl(student.getAvatarKey()));
        dto.setBio(student.getBio());
        dto.setCity(student.getCity());
        dto.setTimezone(student.getTimezone());

        // Private Data
        dto.setEmail(student.getUser().getEmail());
        dto.setPhone(student.getPhone());

        // Stats
        long completed = bookingRepository.countByStudentUserIdAndStatus(studentUserId, BookingStatus.COMPLETED);
        long cancelled = bookingRepository.countByStudentUserIdAndStatusIn(studentUserId, Set.of(BookingStatus.CANCELLED_BY_STUDENT, BookingStatus.CANCELLED_BY_MENTOR));
        dto.setTotalCompletedLessons(completed);
        dto.setTotalCancelledLessons(cancelled);

        // Calculate total hours exactly from DB if possible, but we can do it in Java
        List<Object[]> times = bookingRepository.findStartAndEndTimesByStudentUserIdAndStatus(studentUserId, BookingStatus.COMPLETED);
        int totalHours = 0;
        for (Object[] time : times) {
            if (time[0] instanceof LocalDateTime start && time[1] instanceof LocalDateTime end) {
                totalHours += Duration.between(start, end).toHours();
            }
        }
        dto.setTotalLearningHours(totalHours);

        // Specific History with this mentor
        List<Booking> interactions = bookingRepository.findAllByStudentUserIdAndMentorUserIdOrderByStartAtDesc(studentUserId, mentorUserId);
        List<BookingHistoryDto> history = interactions.stream().map(b -> {
            BookingHistoryDto h = new BookingHistoryDto();
            h.setBookingId(b.getId());
            h.setStartAt(b.getStartAt());
            h.setEndAt(b.getEndAt());
            h.setStatus(b.getStatus().name());

            // Prefer mentor note, otherwise student note
            String note = b.getMentorNote();
            if (note == null || note.isBlank()) {
                note = b.getStudentNote();
            }
            h.setTopicOrNote(note);
            return h;
        }).collect(Collectors.toList());

        dto.setInteractionHistory(history);

        return dto;
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }
}

