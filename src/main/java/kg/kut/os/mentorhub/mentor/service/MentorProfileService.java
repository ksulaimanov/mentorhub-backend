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

    public MentorProfileResponse getByEmail(String email) {
        MentorProfile profile = mentorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Профиль ментора не найден для: " + email));

        return map(profile);
    }

    public MentorProfileResponse updateByEmail(String email, UpdateMentorProfileRequest request) {
        MentorProfile profile = mentorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Профиль ментора не найден"));

        applyUpdates(profile, request);
        return map(profile);
    }

    private void applyUpdates(MentorProfile profile, UpdateMentorProfileRequest request) {
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setAvatarKey(request.getAvatarKey());
        profile.setHeadline(request.getHeadline());
        profile.setBio(request.getBio());
        profile.setSpecialization(request.getSpecialization());
        profile.setYearsExperience(request.getYearsExperience());
        profile.setCity(request.getCity());
        profile.setAddressText(request.getAddressText());
        profile.setMeetingLink(request.getMeetingLink());
        profile.setPricePerHour(request.getPricePerHour());

        // Boolean wrapper fields: only update when explicitly provided (non-null),
        // so that omitting the field in JSON preserves the current value.
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
        response.setReviewCount((int) reviewRepository.countByMentorId(profile.getId()));
        response.setVerified(profile.isVerified());
        response.setPublic(profile.isPublic());
        return response;
    }
}