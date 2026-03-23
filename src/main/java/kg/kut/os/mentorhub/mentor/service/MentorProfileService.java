package kg.kut.os.mentorhub.mentor.service;

import kg.kut.os.mentorhub.common.exception.BadRequestException;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.mentor.dto.MentorProfileResponse;
import kg.kut.os.mentorhub.mentor.dto.UpdateMentorProfileRequest;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;
    private final StorageService storageService;

    public MentorProfileService(MentorProfileRepository mentorProfileRepository, StorageService storageService) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.storageService = storageService;
    }

    public MentorProfileResponse getByUserId(Long userId) {
        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Профиль ментора не найден"));

        return map(profile);
    }

    public MentorProfileResponse update(Long userId, UpdateMentorProfileRequest request) {
        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Профиль ментора не найден"));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setAvatarKey(request.getAvatarKey());
        profile.setHeadline(request.getHeadline());
        profile.setBio(request.getBio());
        profile.setSpecialization(request.getSpecialization());
        profile.setYearsExperience(request.getYearsExperience());
        profile.setLessonFormatOnline(request.isLessonFormatOnline());
        profile.setLessonFormatOffline(request.isLessonFormatOffline());
        profile.setLessonFormatHybrid(request.isLessonFormatHybrid());
        profile.setCity(request.getCity());
        profile.setAddressText(request.getAddressText());
        profile.setMeetingLink(request.getMeetingLink());
        profile.setPricePerHour(request.getPricePerHour());
        profile.setPublic(request.isPublic());

        return map(profile);
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
        response.setVerified(profile.isVerified());
        response.setPublic(profile.isPublic());
        return response;
    }
}