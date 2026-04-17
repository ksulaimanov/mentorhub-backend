package kg.kut.os.mentorhub.auth.service;

import kg.kut.os.mentorhub.auth.dto.UserMeResponse;
import kg.kut.os.mentorhub.auth.entity.RoleCode;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final StudentProfileRepository studentProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final StorageService storageService;

    public UserService(
            StudentProfileRepository studentProfileRepository,
            MentorProfileRepository mentorProfileRepository,
            StorageService storageService) {
        this.studentProfileRepository = studentProfileRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.storageService = storageService;
    }

    public UserMeResponse getUserMeInfo(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getCode().name())
                .collect(Collectors.toSet());

        UserMeResponse response = new UserMeResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRoles(roles);

        if (roles.contains(RoleCode.ROLE_ADMIN.name())) {
            response.setRole(RoleCode.ROLE_ADMIN.name());
            // Admin default values if any
        }

        if (roles.contains(RoleCode.ROLE_MENTOR.name())) {
            response.setRole(RoleCode.ROLE_MENTOR.name());
            mentorProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                response.setFirstName(profile.getFirstName());
                response.setLastName(profile.getLastName());
                response.setAvatarUrl(storageService.buildPublicUrl(profile.getAvatarKey()));
            });
        } else if (roles.contains(RoleCode.ROLE_STUDENT.name())) {
            response.setRole(RoleCode.ROLE_STUDENT.name());
            studentProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                response.setFirstName(profile.getFirstName());
                response.setLastName(profile.getLastName());
                response.setAvatarUrl(storageService.buildPublicUrl(profile.getAvatarKey()));
            });
        }

        return response;
    }
}
