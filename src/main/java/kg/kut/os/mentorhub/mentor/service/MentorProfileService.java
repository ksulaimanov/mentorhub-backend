package kg.kut.os.mentorhub.mentor.service;

import kg.kut.os.mentorhub.common.exception.NotFoundException;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.mentor.dto.MentorProfileResponse;
import kg.kut.os.mentorhub.mentor.dto.UpdateMentorProfileRequest;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import kg.kut.os.mentorhub.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class MentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;
    private final ReviewRepository reviewRepository;
    private final StorageService storageService;

    public MentorProfileService(MentorProfileRepository mentorProfileRepository,
                                ReviewRepository reviewRepository,
                                StorageService storageService) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.reviewRepository = reviewRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public MentorProfileResponse getByUserId(Long userId) {
        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Профиль ментора не найден"));

        return map(profile);
    }

    public MentorProfileResponse update(Long userId, UpdateMentorProfileRequest request) {
        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Профиль ментора не найден"));

        applyUpdates(profile, request);
        return map(profile);
    }

    @Transactional(readOnly = true)
    public MentorProfileResponse getByEmail(String email) {
        MentorProfile profile = mentorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Профиль ментора не найден"));

        return map(profile);
    }

    public MentorProfileResponse updateByEmail(String email, UpdateMentorProfileRequest request) {
        MentorProfile profile = mentorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Профиль ментора не найден"));

        applyUpdates(profile, request);
        return map(profile);
    }

    /**
     * Partial update: only non-null fields from the request override existing values.
     * This prevents data loss when frontend sends only the fields being edited.
     * avatarKey is NOT writable here — use the avatar upload endpoint.
     */
    private void applyUpdates(MentorProfile profile, UpdateMentorProfileRequest request) {
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName().isBlank() ? null : request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName().isBlank() ? null : request.getLastName().trim());
        }
        if (request.getHeadline() != null) {
            profile.setHeadline(request.getHeadline().isBlank() ? null : request.getHeadline().trim());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio().isBlank() ? null : request.getBio().trim());
        }
        if (request.getSpecialization() != null) {
            profile.setSpecialization(request.getSpecialization().isBlank() ? null : request.getSpecialization().trim());
        }
        if (request.getYearsExperience() != null) {
            profile.setYearsExperience(request.getYearsExperience());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity().isBlank() ? null : request.getCity().trim());
        }
        if (request.getAddressText() != null) {
            profile.setAddressText(request.getAddressText().isBlank() ? null : request.getAddressText().trim());
        }
        if (request.getMeetingLink() != null) {
            profile.setMeetingLink(request.getMeetingLink().isBlank() ? null : request.getMeetingLink().trim());
        }
        if (request.getPricePerHour() != null) {
            profile.setPricePerHour(request.getPricePerHour());
        }
        if (request.getLessonFormatOnline() != null) {
            profile.setLessonFormatOnline(request.getLessonFormatOnline());
        }
        if (request.getLessonFormatOffline() != null) {
            profile.setLessonFormatOffline(request.getLessonFormatOffline());
        }
        if (request.getLessonFormatHybrid() != null) {
            profile.setLessonFormatHybrid(request.getLessonFormatHybrid());
        }
        if (request.getIsPublic() != null) {
            profile.setPublic(request.getIsPublic());
        }
    }

    private MentorProfileResponse map(MentorProfile profile) {
        MentorProfileResponse response = new MentorProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setEmail(profile.getUser().getEmail());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());

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
        response.setReviewCount((int) reviewRepository.countByMentorId(profile.getId()));
        response.setVerified(profile.isVerified());
        response.setPublic(profile.isPublic());
        response.setCreatedAt(profile.getCreatedAt());

        // Profile completeness signals for frontend
        response.setProfileComplete(isProfileComplete(profile));
        response.setMissingFields(computeMissingFields(profile));

        return response;
    }

    private boolean isProfileComplete(MentorProfile p) {
        return hasText(p.getFirstName())
                && hasText(p.getLastName())
                && hasText(p.getHeadline())
                && hasText(p.getBio())
                && hasText(p.getSpecialization())
                && (p.isLessonFormatOnline() || p.isLessonFormatOffline() || p.isLessonFormatHybrid());
    }

    private List<String> computeMissingFields(MentorProfile p) {
        List<String> missing = new ArrayList<>();
        if (!hasText(p.getFirstName())) missing.add("firstName");
        if (!hasText(p.getLastName())) missing.add("lastName");
        if (!hasText(p.getHeadline())) missing.add("headline");
        if (!hasText(p.getBio())) missing.add("bio");
        if (!hasText(p.getSpecialization())) missing.add("specialization");
        if (!p.isLessonFormatOnline() && !p.isLessonFormatOffline() && !p.isLessonFormatHybrid()) {
            missing.add("lessonFormat");
        }
        return missing;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}