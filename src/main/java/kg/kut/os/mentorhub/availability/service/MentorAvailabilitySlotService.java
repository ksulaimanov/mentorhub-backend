package kg.kut.os.mentorhub.availability.service;

import kg.kut.os.mentorhub.availability.dto.AvailabilitySlotResponse;
import kg.kut.os.mentorhub.availability.dto.CreateAvailabilitySlotRequest;
import kg.kut.os.mentorhub.availability.dto.UpdateAvailabilitySlotRequest;
import kg.kut.os.mentorhub.availability.entity.LessonFormat;
import kg.kut.os.mentorhub.availability.entity.MentorAvailabilitySlot;
import kg.kut.os.mentorhub.availability.repository.MentorAvailabilitySlotRepository;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import kg.kut.os.mentorhub.booking.repository.BookingRepository;
import kg.kut.os.mentorhub.common.exception.BadRequestException;
import kg.kut.os.mentorhub.common.exception.ConflictException;
import kg.kut.os.mentorhub.common.exception.NotFoundException;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class MentorAvailabilitySlotService {

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorAvailabilitySlotRepository slotRepository;
    private final BookingRepository bookingRepository;

    public MentorAvailabilitySlotService(
            MentorProfileRepository mentorProfileRepository,
            MentorAvailabilitySlotRepository slotRepository,
            BookingRepository bookingRepository
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
    }

    public AvailabilitySlotResponse create(Long mentorUserId, CreateAvailabilitySlotRequest request) {
        validateTimeRange(request.getStartAt(), request.getEndAt());
        validateFormatSpecificFields(request.getLessonFormat(), request.getMeetingLink(), request.getAddressText());

        if (request.getCapacity() == null || request.getCapacity() < 1) {
            throw new BadRequestException("Количество мест должно быть не меньше 1");
        }

        if (!request.getStartAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Нельзя создать слот в прошлом");
        }

        MentorProfile mentor = mentorProfileRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new NotFoundException("Профиль ментора не найден"));

        // Overlap check
        boolean overlaps = slotRepository.existsOverlapping(
                mentor.getId(), request.getStartAt(), request.getEndAt(), null
        );
        if (overlaps) {
            throw new ConflictException("Слот пересекается с уже существующим слотом");
        }

        MentorAvailabilitySlot slot = new MentorAvailabilitySlot();
        slot.setMentor(mentor);
        slot.setStartAt(request.getStartAt());
        slot.setEndAt(request.getEndAt());
        slot.setTimezone(request.getTimezone());
        slot.setLessonFormat(request.getLessonFormat());
        slot.setMeetingLink(request.getMeetingLink());
        slot.setAddressText(request.getAddressText());
        slot.setCapacity(request.getCapacity());
        slot.setActive(true);

        MentorAvailabilitySlot saved = slotRepository.save(slot);
        return mapSingle(saved);
    }

    public List<AvailabilitySlotResponse> getMentorSlots(Long mentorUserId) {
        List<MentorAvailabilitySlot> slots = slotRepository.findAllByMentorUserIdOrderByStartAtAsc(mentorUserId);
        return mapAll(slots);
    }

    public AvailabilitySlotResponse update(Long mentorUserId, Long slotId, UpdateAvailabilitySlotRequest request) {
        validateTimeRange(request.getStartAt(), request.getEndAt());
        validateFormatSpecificFields(request.getLessonFormat(), request.getMeetingLink(), request.getAddressText());

        if (request.getCapacity() == null || request.getCapacity() < 1) {
            throw new BadRequestException("Количество мест должно быть не меньше 1");
        }

        MentorAvailabilitySlot slot = slotRepository.findByIdAndMentorUserId(slotId, mentorUserId)
                .orElseThrow(() -> new NotFoundException("Слот ментора не найден"));

        // Overlap check (exclude self)
        boolean overlaps = slotRepository.existsOverlapping(
                slot.getMentor().getId(), request.getStartAt(), request.getEndAt(), slot.getId()
        );
        if (overlaps) {
            throw new ConflictException("Слот пересекается с уже существующим слотом");
        }

        long bookedCount = bookingRepository.countByAvailabilitySlotIdAndStatusIn(
                slot.getId(),
                ACTIVE_BOOKING_STATUSES
        );

        if (request.getCapacity() < bookedCount) {
            throw new BadRequestException("Нельзя уменьшить количество мест ниже числа активных записей");
        }

        slot.setStartAt(request.getStartAt());
        slot.setEndAt(request.getEndAt());
        slot.setTimezone(request.getTimezone());
        slot.setLessonFormat(request.getLessonFormat());
        slot.setMeetingLink(request.getMeetingLink());
        slot.setAddressText(request.getAddressText());
        slot.setCapacity(request.getCapacity());
        slot.setActive(request.isActive());

        return mapSingle(slot);
    }

    public void deactivate(Long mentorUserId, Long slotId) {
        MentorAvailabilitySlot slot = slotRepository.findByIdAndMentorUserId(slotId, mentorUserId)
                .orElseThrow(() -> new NotFoundException("Слот ментора не найден"));

        slot.setActive(false);
    }

    public List<AvailabilitySlotResponse> getPublicSlots(Long mentorProfileId) {
        List<MentorAvailabilitySlot> slots = slotRepository
                .findByMentorIdAndIsActiveTrueAndStartAtAfterOrderByStartAtAsc(
                        mentorProfileId, LocalDateTime.now());
        return mapAll(slots);
    }

    // ----------------------------------------------------------------
    // Validation helpers
    // ----------------------------------------------------------------

    private void validateTimeRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
            throw new BadRequestException("Время окончания должно быть позже времени начала");
        }
    }

    private void validateFormatSpecificFields(LessonFormat format, String meetingLink, String addressText) {
        if (format == LessonFormat.ONLINE && (meetingLink == null || meetingLink.isBlank())) {
            throw new BadRequestException("Для онлайн-урока нужно указать ссылку на встречу");
        }

        if (format == LessonFormat.OFFLINE && (addressText == null || addressText.isBlank())) {
            throw new BadRequestException("Для офлайн-урока нужно указать адрес");
        }

        if (format == LessonFormat.HYBRID) {
            if ((meetingLink == null || meetingLink.isBlank()) && (addressText == null || addressText.isBlank())) {
                throw new BadRequestException("Для гибридного урока нужно указать ссылку или адрес");
            }
        }
    }

    // ----------------------------------------------------------------
    // Mapping helpers — batch count to avoid N+1
    // ----------------------------------------------------------------

    /**
     * Maps a list of slots using a single batch count query for booked counts.
     */
    private List<AvailabilitySlotResponse> mapAll(List<MentorAvailabilitySlot> slots) {
        if (slots.isEmpty()) {
            return List.of();
        }

        List<Long> slotIds = slots.stream().map(MentorAvailabilitySlot::getId).toList();
        Map<Long, Long> bookedCountMap = bookingRepository
                .countBySlotIdsAndStatusIn(slotIds, ACTIVE_BOOKING_STATUSES)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return slots.stream()
                .map(slot -> mapSlot(slot, bookedCountMap.getOrDefault(slot.getId(), 0L)))
                .toList();
    }

    /**
     * Maps a single slot (used after create/update where batch is unnecessary).
     */
    private AvailabilitySlotResponse mapSingle(MentorAvailabilitySlot slot) {
        long bookedCount = bookingRepository.countByAvailabilitySlotIdAndStatusIn(
                slot.getId(), ACTIVE_BOOKING_STATUSES);
        return mapSlot(slot, bookedCount);
    }

    private AvailabilitySlotResponse mapSlot(MentorAvailabilitySlot slot, long bookedCount) {
        AvailabilitySlotResponse response = new AvailabilitySlotResponse();
        response.setId(slot.getId());
        response.setMentorId(slot.getMentor().getId());
        response.setStartAt(slot.getStartAt());
        response.setEndAt(slot.getEndAt());
        response.setTimezone(slot.getTimezone());
        response.setLessonFormat(slot.getLessonFormat());
        response.setMeetingLink(slot.getMeetingLink());
        response.setAddressText(slot.getAddressText());
        response.setActive(slot.isActive());
        response.setCapacity(slot.getCapacity());
        response.setBookedCount((int) bookedCount);
        response.setAvailableSeats(Math.max(slot.getCapacity() - (int) bookedCount, 0));

        return response;
    }
}