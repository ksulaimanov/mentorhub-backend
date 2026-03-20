package kg.kut.os.mentorhub.mentor.service;

import kg.kut.os.mentorhub.common.exception.BadRequestException;
import kg.kut.os.mentorhub.mentor.dto.MentorDirectoryFilter;
import kg.kut.os.mentorhub.mentor.dto.MentorDirectoryItemResponse;
import kg.kut.os.mentorhub.mentor.dto.PublicMentorProfileResponse;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PublicMentorDirectoryService {

    private final MentorProfileRepository mentorProfileRepository;

    public PublicMentorDirectoryService(MentorProfileRepository mentorProfileRepository) {
        this.mentorProfileRepository = mentorProfileRepository;
    }

    public List<MentorDirectoryItemResponse> getDirectory(MentorDirectoryFilter filter) {
        Sort sort = resolveSort(filter.getSortBy());

        return mentorProfileRepository.findAllByIsPublicTrue(sort)
                .stream()
                .filter(mentor -> matchesQuery(mentor, filter.getQuery()))
                .filter(mentor -> matchesSpecialization(mentor, filter.getSpecialization()))
                .filter(mentor -> matchesCity(mentor, filter.getCity()))
                .filter(mentor -> matchesFormats(mentor, filter.getOnline(), filter.getOffline(), filter.getHybrid()))
                .map(this::toDirectoryItem)
                .collect(Collectors.toList());
    }

    public PublicMentorProfileResponse getPublicProfile(Long mentorId) {
        MentorProfile mentor = mentorProfileRepository.findByIdAndIsPublicTrue(mentorId)
                .orElseThrow(() -> new BadRequestException("Публичный профиль ментора не найден"));

        return toPublicProfile(mentor);
    }

    private boolean matchesQuery(MentorProfile mentor, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);

        return contains(mentor.getFirstName(), normalizedQuery)
                || contains(mentor.getLastName(), normalizedQuery)
                || contains(mentor.getHeadline(), normalizedQuery)
                || contains(mentor.getSpecialization(), normalizedQuery);
    }

    private boolean matchesSpecialization(MentorProfile mentor, String specialization) {
        if (specialization == null || specialization.isBlank()) {
            return true;
        }

        return contains(mentor.getSpecialization(), specialization.trim().toLowerCase(Locale.ROOT));
    }

    private boolean matchesCity(MentorProfile mentor, String city) {
        if (city == null || city.isBlank()) {
            return true;
        }

        return contains(mentor.getCity(), city.trim().toLowerCase(Locale.ROOT));
    }

    private boolean matchesFormats(MentorProfile mentor, Boolean online, Boolean offline, Boolean hybrid) {
        if (Boolean.TRUE.equals(online) && !mentor.isLessonFormatOnline()) {
            return false;
        }

        if (Boolean.TRUE.equals(offline) && !mentor.isLessonFormatOffline()) {
            return false;
        }

        if (Boolean.TRUE.equals(hybrid) && !mentor.isLessonFormatHybrid()) {
            return false;
        }

        return true;
    }

    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "averageRating")
                    .and(Sort.by(Sort.Direction.DESC, "lessonsCompleted"));
        }

        return switch (sortBy) {
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "pricePerHour");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "pricePerHour");
            case "ratingDesc" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "experienceDesc" -> Sort.by(Sort.Direction.DESC, "yearsExperience");
            default -> Sort.by(Sort.Direction.DESC, "averageRating")
                    .and(Sort.by(Sort.Direction.DESC, "lessonsCompleted"));
        };
    }

    private MentorDirectoryItemResponse toDirectoryItem(MentorProfile mentor) {
        MentorDirectoryItemResponse response = new MentorDirectoryItemResponse();
        response.setId(mentor.getId());
        response.setUserId(mentor.getUser().getId());
        response.setFirstName(mentor.getFirstName());
        response.setLastName(mentor.getLastName());
        response.setAvatarKey(mentor.getAvatarKey());
        response.setHeadline(mentor.getHeadline());
        response.setSpecialization(mentor.getSpecialization());
        response.setYearsExperience(mentor.getYearsExperience());
        response.setLessonFormatOnline(mentor.isLessonFormatOnline());
        response.setLessonFormatOffline(mentor.isLessonFormatOffline());
        response.setLessonFormatHybrid(mentor.isLessonFormatHybrid());
        response.setCity(mentor.getCity());
        response.setPricePerHour(mentor.getPricePerHour());
        response.setAverageRating(mentor.getAverageRating());
        response.setLessonsCompleted(mentor.getLessonsCompleted());
        response.setVerified(mentor.isVerified());
        return response;
    }

    private PublicMentorProfileResponse toPublicProfile(MentorProfile mentor) {
        PublicMentorProfileResponse response = new PublicMentorProfileResponse();
        response.setId(mentor.getId());
        response.setUserId(mentor.getUser().getId());
        response.setFirstName(mentor.getFirstName());
        response.setLastName(mentor.getLastName());
        response.setAvatarKey(mentor.getAvatarKey());
        response.setHeadline(mentor.getHeadline());
        response.setBio(mentor.getBio());
        response.setSpecialization(mentor.getSpecialization());
        response.setYearsExperience(mentor.getYearsExperience());
        response.setLessonFormatOnline(mentor.isLessonFormatOnline());
        response.setLessonFormatOffline(mentor.isLessonFormatOffline());
        response.setLessonFormatHybrid(mentor.isLessonFormatHybrid());
        response.setCity(mentor.getCity());
        response.setAddressText(mentor.getAddressText());
        response.setMeetingLink(mentor.getMeetingLink());
        response.setPricePerHour(mentor.getPricePerHour());
        response.setAverageRating(mentor.getAverageRating());
        response.setLessonsCompleted(mentor.getLessonsCompleted());
        response.setVerified(mentor.isVerified());
        return response;
    }
}