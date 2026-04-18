package kg.kut.os.mentorhub.mentor.service;

import kg.kut.os.mentorhub.availability.dto.PublicAvailabilitySlotResponse;
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
import kg.kut.os.mentorhub.review.dto.ReviewResponse;
import kg.kut.os.mentorhub.review.entity.Review;
import kg.kut.os.mentorhub.review.repository.ReviewRepository;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class PublicMentorDirectoryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_PUBLIC_SLOTS = 30;

    private static final Set<String> ALLOWED_SORT_VALUES = Set.of(
            "ratingDesc", "priceAsc", "priceDesc", "experienceDesc", "newest"
    );

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorAvailabilitySlotRepository mentorAvailabilitySlotRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final StorageService storageService;

    public PublicMentorDirectoryService(
            MentorProfileRepository mentorProfileRepository,
            MentorAvailabilitySlotRepository mentorAvailabilitySlotRepository,
            BookingRepository bookingRepository,
            ReviewRepository reviewRepository,
            StorageService storageService
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.mentorAvailabilitySlotRepository = mentorAvailabilitySlotRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.storageService = storageService;
    }

    // ----------------------------------------------------------------
    // Directory (paginated)
    // ----------------------------------------------------------------

    public Page<MentorDirectoryItemResponse> getDirectory(MentorDirectoryFilter filter, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        String sanitizedSort = sanitizeSortBy(filter == null ? null : filter.getSortBy());
        Sort sort = resolveSort(sanitizedSort);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, sort);
        org.springframework.data.jpa.domain.Specification<MentorProfile> spec = (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
            }
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.isTrue(root.get("isPublic")));
            if (filter != null) {
                if (hasText(filter.getQuery())) {
                    String q = "%" + filter.getQuery().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("firstName")), q),
                            cb.like(cb.lower(root.get("lastName")), q),
                            cb.like(cb.lower(root.get("headline")), q),
                            cb.like(cb.lower(root.get("specialization")), q)
                    ));
                }
                if (hasText(filter.getSpecialization())) {
                    predicates.add(cb.like(cb.lower(root.get("specialization")), "%" + filter.getSpecialization().toLowerCase() + "%"));
                }
                if (hasText(filter.getCity())) {
                    predicates.add(cb.like(cb.lower(root.get("city")), "%" + filter.getCity().toLowerCase() + "%"));
                }
                if (Boolean.TRUE.equals(filter.getOnline())) {
                    predicates.add(cb.isTrue(root.get("lessonFormatOnline")));
                }
                if (Boolean.TRUE.equals(filter.getOffline())) {
                    predicates.add(cb.isTrue(root.get("lessonFormatOffline")));
                }
                if (Boolean.TRUE.equals(filter.getHybrid())) {
                    predicates.add(cb.isTrue(root.get("lessonFormatHybrid")));
                }
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<MentorProfile> pageSlice = mentorProfileRepository.findAll(spec, pageRequest);
        if (!pageSlice.hasContent()) {
            return new PageImpl<>(Collections.emptyList(), pageRequest, pageSlice.getTotalElements());
        }
        List<Long> mentorIds = pageSlice.getContent().stream().map(MentorProfile::getId).toList();
        Map<Long, Long> reviewCountMap = batchReviewCounts(mentorIds);
        List<MentorDirectoryItemResponse> items = pageSlice.getContent().stream()
                .map(profile -> mapDirectoryItem(profile, reviewCountMap))
                .toList();
        return new PageImpl<>(items, pageRequest, pageSlice.getTotalElements());
    }
    private static final int LATEST_REVIEWS_LIMIT = 5;
    // ----------------------------------------------------------------
    // Public mentor profile
    // ----------------------------------------------------------------

    public PublicMentorProfileResponse getPublicProfile(Long mentorId) {
        MentorProfile profile = mentorProfileRepository.findByIdAndIsPublicTrue(mentorId)
                .orElseThrow(() -> new NotFoundException("Публичный профиль ментора не найден"));

        int reviewCount = (int) reviewRepository.countByMentorId(profile.getId());
        boolean hasSlots = mentorAvailabilitySlotRepository
                .countFutureActiveSlots(profile.getId(), LocalDateTime.now()) > 0;

        // Fetch latest reviews for trust UI (avoid extra API call from frontend)
        Page<Review> latestPage = reviewRepository.findAllByMentorIdOrderByCreatedAtDesc(
                profile.getId(), PageRequest.of(0, LATEST_REVIEWS_LIMIT)
        );
        List<ReviewResponse> latestReviews = latestPage.getContent().stream()
                .map(this::mapReview)
                .toList();

        PublicMentorProfileResponse response = mapPublicProfile(profile, reviewCount, hasSlots);
        response.setLatestReviews(latestReviews);
        return response;
    }

    // ----------------------------------------------------------------
    // Public availability slots
    // ----------------------------------------------------------------

    public List<PublicAvailabilitySlotResponse> getPublicSlots(Long mentorId) {
        MentorProfile profile = mentorProfileRepository.findByIdAndIsPublicTrue(mentorId)
                .orElseThrow(() -> new NotFoundException("Публичный профиль ментора не найден"));

        List<MentorAvailabilitySlot> slots = mentorAvailabilitySlotRepository
                .findByMentorIdAndIsActiveTrueAndStartAtAfterOrderByStartAtAsc(
                        profile.getId(),
                        LocalDateTime.now()
                );

        if (slots.isEmpty()) {
            return List.of();
        }

        // Limit to reasonable number for public page
        List<MentorAvailabilitySlot> limitedSlots = slots.size() > MAX_PUBLIC_SLOTS
                ? slots.subList(0, MAX_PUBLIC_SLOTS)
                : slots;

        List<Long> slotIds = limitedSlots.stream().map(MentorAvailabilitySlot::getId).toList();
        Map<Long, Long> bookedCountMap = bookingRepository
                .countBySlotIdsAndStatusIn(slotIds, ACTIVE_BOOKING_STATUSES)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return limitedSlots.stream()
                .map(slot -> mapPublicSlot(slot, bookedCountMap))
                .toList();
    }

    // ----------------------------------------------------------------
    // Mapping helpers
    // ----------------------------------------------------------------

    private MentorDirectoryItemResponse mapDirectoryItem(MentorProfile profile, Map<Long, Long> reviewCountMap) {
        MentorDirectoryItemResponse response = new MentorDirectoryItemResponse();
        response.setId(profile.getId());
        response.setFirstName(safeString(profile.getFirstName()));
        response.setLastName(safeString(profile.getLastName()));
        response.setDisplayName(buildDisplayName(profile));
        response.setAvatarUrl(safeAvatarUrl(profile.getAvatarKey()));
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
        response.setReviewCount(reviewCountMap.getOrDefault(profile.getId(), 0L).intValue());
        response.setVerified(profile.isVerified());
        return response;
    }

    private PublicMentorProfileResponse mapPublicProfile(MentorProfile profile, int reviewCount, boolean hasAvailableSlots) {
        PublicMentorProfileResponse response = new PublicMentorProfileResponse();
        response.setId(profile.getId());
        response.setFirstName(safeString(profile.getFirstName()));
        response.setLastName(safeString(profile.getLastName()));
        response.setDisplayName(buildDisplayName(profile));
        response.setAvatarUrl(safeAvatarUrl(profile.getAvatarKey()));
        response.setHeadline(profile.getHeadline());
        response.setBio(profile.getBio());
        response.setSpecialization(profile.getSpecialization());
        response.setYearsExperience(profile.getYearsExperience());
        response.setLessonFormatOnline(profile.isLessonFormatOnline());
        response.setLessonFormatOffline(profile.isLessonFormatOffline());
        response.setLessonFormatHybrid(profile.isLessonFormatHybrid());
        response.setCity(profile.getCity());
        response.setPricePerHour(profile.getPricePerHour());
        response.setAverageRating(profile.getAverageRating());
        response.setLessonsCompleted(profile.getLessonsCompleted());
        response.setReviewCount(reviewCount);
        response.setMemberSince(profile.getCreatedAt() != null ? profile.getCreatedAt().toString() : null);
        response.setVerified(profile.isVerified());
        response.setHasAvailableSlots(hasAvailableSlots);
        response.setInstagramUrl(profile.getInstagramUrl());
        response.setTelegramUsername(profile.getTelegramUsername());
        response.setPublicEmail(profile.getPublicEmail());
        return response;
    }

    private PublicAvailabilitySlotResponse mapPublicSlot(MentorAvailabilitySlot slot, Map<Long, Long> bookedCountMap) {
        long bookedCount = bookedCountMap.getOrDefault(slot.getId(), 0L);
        int capacity = slot.getCapacity() != null ? slot.getCapacity() : 1;
        int available = Math.max(capacity - (int) bookedCount, 0);

        PublicAvailabilitySlotResponse response = new PublicAvailabilitySlotResponse();
        response.setId(slot.getId());
        response.setMentorId(slot.getMentor().getId());
        response.setStartAt(slot.getStartAt());
        response.setEndAt(slot.getEndAt());
        response.setTimezone(slot.getTimezone());
        response.setLessonFormat(slot.getLessonFormat());
        response.setCapacity(capacity);
        response.setBookedCount((int) bookedCount);
        response.setAvailableSeats(available);
        response.setBookable(available > 0);
        return response;
    }

    // ----------------------------------------------------------------
    // Batch helpers
    // ----------------------------------------------------------------

    private Map<Long, Long> batchReviewCounts(List<Long> mentorIds) {
        if (mentorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return reviewRepository.countByMentorIds(mentorIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    // ----------------------------------------------------------------
    // Sort
    // ----------------------------------------------------------------

    private String sanitizeSortBy(String sortBy) {
        if (!hasText(sortBy) || !ALLOWED_SORT_VALUES.contains(sortBy)) {
            return null;
        }
        return sortBy;
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) {
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
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "verified")
                    .and(Sort.by(Sort.Direction.DESC, "averageRating"))
                    .and(Sort.by(Sort.Direction.DESC, "lessonsCompleted"));
        };
    }

    // ----------------------------------------------------------------
    // Null-safe helpers
    // ----------------------------------------------------------------

    private String buildDisplayName(MentorProfile profile) {
        String first = safeString(profile.getFirstName());
        String last = safeString(profile.getLastName());
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? "Ментор" : combined;
    }

    private String safeAvatarUrl(String avatarKey) {
        if (avatarKey == null || avatarKey.isBlank()) {
            return null;
        }
        return storageService.buildPublicUrl(avatarKey);
    }

    private String safeString(String value) {
        return value != null ? value : "";
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

    // ----------------------------------------------------------------
    // Review mapping (lightweight — for embedded latest reviews)
    // ----------------------------------------------------------------

    private ReviewResponse mapReview(Review review) {
        StudentProfile student = review.getStudent();

        ReviewResponse r = new ReviewResponse();
        r.setId(review.getId());
        r.setBookingId(review.getBooking().getId());
        r.setMentorId(review.getMentor().getId());
        r.setStudentId(student.getId());
        r.setRating(review.getRating());
        r.setComment(review.getComment());
        r.setCreatedAt(review.getCreatedAt());
        r.setStudentFirstName(student.getFirstName());
        r.setStudentLastName(student.getLastName());
        r.setStudentAvatarUrl(safeAvatarUrl(student.getAvatarKey()));
        return r;
    }
}

