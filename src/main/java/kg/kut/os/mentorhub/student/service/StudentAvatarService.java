package kg.kut.os.mentorhub.student.service;

import jakarta.transaction.Transactional;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.common.dto.AvatarResponse;
import kg.kut.os.mentorhub.common.exception.NotFoundException;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class StudentAvatarService {

    private final StudentProfileRepository studentProfileRepository;
    private final StorageService storageService;

    public StudentAvatarService(
            StudentProfileRepository studentProfileRepository,
            StorageService storageService
    ) {
        this.studentProfileRepository = studentProfileRepository;
        this.storageService = storageService;
    }

    public AvatarResponse uploadAvatar(User currentUser, MultipartFile file) {
        StudentProfile profile = studentProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Профиль студента не найден"));

        String oldAvatarKey = profile.getAvatarKey();
        String newAvatarKey = storageService.uploadAvatar(currentUser.getId(), file);

        profile.setAvatarKey(newAvatarKey);
        studentProfileRepository.save(profile);

        if (oldAvatarKey != null && !oldAvatarKey.isBlank()) {
            storageService.delete(oldAvatarKey);
        }

        return new AvatarResponse(newAvatarKey, storageService.buildPublicUrl(newAvatarKey));
    }

    public void deleteAvatar(User currentUser) {
        StudentProfile profile = studentProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Профиль студента не найден"));

        String oldAvatarKey = profile.getAvatarKey();
        profile.setAvatarKey(null);
        studentProfileRepository.save(profile);

        if (oldAvatarKey != null && !oldAvatarKey.isBlank()) {
            storageService.delete(oldAvatarKey);
        }
    }
}