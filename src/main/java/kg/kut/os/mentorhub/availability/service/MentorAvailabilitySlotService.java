package kg.kut.os.mentorhub.availability.service;

import kg.kut.os.mentorhub.availability.dto.AvailabilitySlotResponse;
import kg.kut.os.mentorhub.availability.dto.CreateAvailabilitySlotRequest;
import kg.kut.os.mentorhub.availability.dto.UpdateAvailabilitySlotRequest;
import kg.kut.os.mentorhub.availability.entity.LessonFormat;
import kg.kut.os.mentorhub.availability.entity.MentorAvailabilitySlot;
import kg.kut.os.mentorhub.availability.repository.MentorAvailabilitySlotRepository;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import kg.kut.os.mentorhub.booking.repository.BookingRepository; // Импорт репозитория
import kg.kut.os.mentorhub.common.exception.BadRequestException;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MentorAvailabilitySlotService {

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

        MentorProfile mentor = mentorProfileRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new BadRequestException("Профиль ментора не найден"));

        MentorAvailabilitySlot slot = new MentorAvailabilitySlot();
        slot.setCapacity(request.getCapacity());
        slot.setMentor(mentor);
        slot.setStartAt(request.getStartAt());
        slot.setEndAt(request.getEndAt());
        slot.setTimezone(request.getTimezone());
        slot.setLessonFormat(request.getLessonFormat());
        slot.setMeetingLink(request.getMeetingLink());
        slot.setAddressText(request.getAddressText());
        slot.setActive(true);

        if (request.getCapacity() == null || request.getCapacity() < 1) {
            throw new BadRequestException("Количество мест должно быть не меньше 1");
        }

        return map(slotRepository.save(slot));
    }

    public List<AvailabilitySlotResponse> getMentorSlots(Long mentorUserId) {
        return slotRepository.findAllByMentorUserIdOrderByStartAtAsc(mentorUserId)
                .stream()
                .map(this::map)
                .toList();
    }

    public AvailabilitySlotResponse update(Long mentorUserId, Long slotId, UpdateAvailabilitySlotRequest request) {
        validateTimeRange(request.getStartAt(), request.getEndAt());
        validateFormatSpecificFields(request.getLessonFormat(), request.getMeetingLink(), request.getAddressText());

        MentorAvailabilitySlot slot = slotRepository.findByIdAndMentorUserId(slotId, mentorUserId)
                .orElseThrow(() -> new BadRequestException("Слот ментора не найден"));

        slot.setStartAt(request.getStartAt());
        slot.setEndAt(request.getEndAt());
        slot.setTimezone(request.getTimezone());
        slot.setLessonFormat(request.getLessonFormat());
        slot.setMeetingLink(request.getMeetingLink());
        slot.setAddressText(request.getAddressText());
        slot.setActive(request.isActive());

        return map(slot);
    }

    public void deactivate(Long mentorUserId, Long slotId) {
        MentorAvailabilitySlot slot = slotRepository.findByIdAndMentorUserId(slotId, mentorUserId)
                .orElseThrow(() -> new BadRequestException("Слот ментора не найден"));

        slot.setActive(false);
    }

    public List<AvailabilitySlotResponse> getPublicSlots(Long mentorProfileId) {
        return slotRepository.findAllByMentorIdAndIsActiveTrueOrderByStartAtAsc(mentorProfileId)
                .stream()
                .map(this::map)
                .toList();
    }

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

    private AvailabilitySlotResponse map(MentorAvailabilitySlot slot) {
        long bookedCount = bookingRepository.countByAvailabilitySlotIdAndStatusIn(
                slot.getId(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

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