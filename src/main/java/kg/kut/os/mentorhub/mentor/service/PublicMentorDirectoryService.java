package kg.kut.os.mentorhub.mentor.service;

import kg.kut.os.mentorhub.availability.dto.AvailabilitySlotResponse;
import kg.kut.os.mentorhub.availability.entity.MentorAvailabilitySlot;
import kg.kut.os.mentorhub.availability.repository.MentorAvailabilitySlotRepository;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import kg.kut.os.mentorhub.booking.repository.BookingRepository;
import kg.kut.os.mentorhub.common.exception.NotFoundException;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.mentor.dto.MentorDirectoryFilter;
import kg.kut.os.mentorhub.mentor.dto.MentorDirectoryItemResponse;
import kg.kut.os.mentorhub.mentor.dto.PublicMentorProfileResponse;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class PublicMentorDirectoryService {

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorAvailabilitySlotRepository mentorAvailabilitySlotRepository;
    private final BookingRepository bookingRepository;
    private final StorageService storageService;

    public PublicMentorDirectoryService(
            MentorProfileRepository mentorProfileRepository,
            MentorAvailabilitySlotRepository mentorAvailabilitySlotRepository,
            BookingRepository bookingRepository,
            StorageService storageService
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.mentorAvailabilitySlotRepository = mentorAvailabilitySlotRepository;
        this.bookingRepository = bookingRepository;
        this.storageService = storageService;
    }

    public List<MentorDirectoryItemResponse> getDirectory(MentorDirectoryFilter filter) {
        Sort sort = resolveSort(filter == null ? null : filter.getSortBy());

        List<MentorProfile> profiles = mentorProfileRepository.findAllByIsPublicTrue(sort);

        Stream<MentorProfile> stream = profiles.stream();

        if (filter != null) {
            if (hasText(filter.getQuery())) {
                String query = normalize(filter.getQuery());

                stream = stream.filter(profile ->
                        contains(profile.getFirstName(), query)
                                || contains(profile.getLastName(), query)
                                || contains(profile.getHeadline(), query)
                                || contains(profile.getSpecialization(), query)
                );
            }

            if (hasText(filter.getSpecialization())) {
                String specialization = normalize(filter.getSpecialization());
                stream = stream.filter(profile -> contains(profile.getSpecialization(), specialization));
            }

            if (hasText(filter.getCity())) {
                String city = normalize(filter.getCity());
                stream = stream.filter(profile -> contains(profile.getCity(), city));
            }

            if (Boolean.TRUE.equals(filter.getOnline())) {
                stream = stream.filter(MentorProfile::isLessonFormatOnline);
            }

            if (Boolean.TRUE.equals(filter.getOffline())) {
                stream = stream.filter(MentorProfile::isLessonFormatOffline);
            }

            if (Boolean.TRUE.equals(filter.getHybrid())) {
                stream = stream.filter(MentorProfile::isLessonFormatHybrid);
            }
        }

        return stream
                .map(this::mapDirectoryItem)
                .toList();
    }

    public PublicMentorProfileResponse getPublicProfile(Long mentorId) {
        MentorProfile profile = mentorProfileRepository.findByIdAndIsPublicTrue(mentorId)
                .orElseThrow(() -> new NotFoundException("Публичный профиль ментора не найден"));

        return mapPublicProfile(profile);
    }

    public List<AvailabilitySlotResponse> getPublicSlots(Long mentorId) {
        MentorProfile profile = mentorProfileRepository.findByIdAndIsPublicTrue(mentorId)
                .orElseThrow(() -> new NotFoundException("Публичный профиль ментора не найден"));

        return mentorAvailabilitySlotRepository
                .findByMentorIdAndIsActiveTrueAndStartAtAfterOrderByStartAtAsc(
                        profile.getId(),
                        LocalDateTime.now()
                )
                .stream()
                .map(this::mapAvailabilitySlot)
                .toList();
    }

    private MentorDirectoryItemResponse mapDirectoryItem(MentorProfile profile) {
        MentorDirectoryItemResponse response = new MentorDirectoryItemResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setAvatarKey(profile.getAvatarKey());
        response.setAvatarUrl(storageService.buildPublicUrl(profile.getAvatarKey()));
        response.setHeadline(profile.getHeadline());
        response.setSpecialization(profile.getSpecialization());
        response.setYearsExperience(profile.getYearsExperience());
        response.setLessonFormatOnline(profile.isLessonFormatOnline());
        response.setLessonFormatOffline(profile.isLessonFormatOffline());
        response.setLessonFormatHybrid(profile.isLessonFormatHybrid());
        response.setCity(profile.getCity());
        response.setPricePerHour(profile.getPricePerHour());
        response.setAverageRating(profile.getAverageRating());
        response.setLessonsCompleted(profile.getLessonsCompleted());
        response.setVerified(profile.isVerified());
        return response;
    }

    private PublicMentorProfileResponse mapPublicProfile(MentorProfile profile) {
        PublicMentorProfileResponse response = new PublicMentorProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setAvatarKey(profile.getAvatarKey());
        response.setAvatarUrl(storageService.buildPublicUrl(profile.getAvatarKey()));
        response.setHeadline(profile.getHeadline());
        response.setBio(profile.getBio());
        response.setSpecialization(profile.getSpecialization());
        response.setYearsExperience(profile.getYearsExperience());
        response.setLessonFormatOnline(profile.isLessonFormatOnline());
        response.setLessonFormatOffline(profile.isLessonFormatOffline());
        response.setLessonFormatHybrid(profile.isLessonFormatHybrid());
        response.setCity(profile.getCity());
        response.setAddressText(profile.getAddressText());
        response.setMeetingLink(profile.getMeetingLink());
        response.setPricePerHour(profile.getPricePerHour());
        response.setAverageRating(profile.getAverageRating());
        response.setLessonsCompleted(profile.getLessonsCompleted());
        response.setVerified(profile.isVerified());
        return response;
    }

    private AvailabilitySlotResponse mapAvailabilitySlot(MentorAvailabilitySlot slot) {
        long bookedCount = bookingRepository.countByAvailabilitySlotIdAndStatusIn(
                slot.getId(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );
        int capacity = slot.getCapacity();
        int available = Math.max(capacity - (int) bookedCount, 0);

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
        response.setCapacity(capacity);
        response.setBookedCount((int) bookedCount);
        response.setAvailableSeats(available);
        return response;
    }

    private Sort resolveSort(String sortBy) {
        if (!hasText(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "verified")
                    .and(Sort.by(Sort.Direction.DESC, "averageRating"))
                    .and(Sort.by(Sort.Direction.DESC, "lessonsCompleted"));
        }

        return switch (sortBy) {
            case "ratingDesc" -> Sort.by(Sort.Direction.DESC, "averageRating")
                    .and(Sort.by(Sort.Direction.DESC, "lessonsCompleted"));
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "pricePerHour");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "pricePerHour");
            case "experienceDesc" -> Sort.by(Sort.Direction.DESC, "yearsExperience");
            default -> Sort.by(Sort.Direction.DESC, "verified")
                    .and(Sort.by(Sort.Direction.DESC, "averageRating"))
                    .and(Sort.by(Sort.Direction.DESC, "lessonsCompleted"));
        };
    }

    private boolean contains(String source, String query) {
        return source != null && normalize(source).contains(query);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}