package kg.kut.os.mentorhub.mentor.service;

import jakarta.transaction.Transactional;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.common.dto.AvatarResponse;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MentorAvatarService {

    private final MentorProfileRepository mentorProfileRepository;
    private final StorageService storageService;

    public MentorAvatarService(
            MentorProfileRepository mentorProfileRepository,
            StorageService storageService
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.storageService = storageService;
    }

    public AvatarResponse uploadAvatar(User currentUser, MultipartFile file) {
        MentorProfile profile = mentorProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Профиль ментора не найден"));

        String oldAvatarKey = profile.getAvatarKey();
        String newAvatarKey = storageService.uploadAvatar(currentUser.getId(), file);

        profile.setAvatarKey(newAvatarKey);
        mentorProfileRepository.save(profile);

        if (oldAvatarKey != null && !oldAvatarKey.isBlank()) {
            storageService.delete(oldAvatarKey);
        }

        return new AvatarResponse(newAvatarKey, storageService.buildPublicUrl(newAvatarKey));
    }

    public void deleteAvatar(User currentUser) {
        MentorProfile profile = mentorProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Профиль ментора не найден"));

        String oldAvatarKey = profile.getAvatarKey();
        profile.setAvatarKey(null);
        mentorProfileRepository.save(profile);

        if (oldAvatarKey != null && !oldAvatarKey.isBlank()) {
            storageService.delete(oldAvatarKey);
        }
    }
}