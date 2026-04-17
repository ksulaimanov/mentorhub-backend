package kg.kut.os.mentorhub.auth.service;

import kg.kut.os.mentorhub.auth.dto.AdminUserSummaryDto;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.auth.entity.UserStatus;
import kg.kut.os.mentorhub.auth.repository.RefreshTokenRepository;
import kg.kut.os.mentorhub.auth.repository.UserRepository;
import kg.kut.os.mentorhub.common.exception.NotFoundException;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StorageService storageService;

    public AdminUserService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            MentorProfileRepository mentorProfileRepository,
            StudentProfileRepository studentProfileRepository,
            StorageService storageService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserSummaryDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToSummary);
    }

    public void changeStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        user.setStatus(status);
        userRepository.save(user);

        if (status == UserStatus.BLOCKED) {
            refreshTokenRepository.findAllByUserId(userId)
                    .forEach(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }
    }

    public void deleteAvatar(Long userId) {
        mentorProfileRepository.findByUserId(userId).ifPresent(profile -> {
            String key = profile.getAvatarKey();
            if (key != null && !key.isBlank()) {
                storageService.delete(key);
                profile.setAvatarKey(null);
                mentorProfileRepository.save(profile);
            }
        });

        studentProfileRepository.findByUserId(userId).ifPresent(profile -> {
            String key = profile.getAvatarKey();
            if (key != null && !key.isBlank()) {
                storageService.delete(key);
                profile.setAvatarKey(null);
                studentProfileRepository.save(profile);
            }
        });
    }

    private AdminUserSummaryDto mapToSummary(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getCode().name())
                .collect(Collectors.toSet());
        return new AdminUserSummaryDto(
                user.getId(),
                user.getEmail(),
                roles,
                user.getStatus(),
                user.getCreatedAt(),
                user.getLastActiveAt()
        );
    }
}

