package kg.kut.os.mentorhub.student.service;

import kg.kut.os.mentorhub.auth.repository.UserRepository;
import kg.kut.os.mentorhub.common.exception.NotFoundException;
import kg.kut.os.mentorhub.common.util.LocaleUtils;
import kg.kut.os.mentorhub.media.StorageService;
import kg.kut.os.mentorhub.student.dto.StudentProfileResponse;
import kg.kut.os.mentorhub.student.dto.UpdateStudentProfileRequest;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class StudentProfileService {

    private static final int STUDENT_TOTAL_FIELDS = 4; // firstName, lastName, bio, avatar

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public StudentProfileService(StudentProfileRepository studentProfileRepository,
                                 UserRepository userRepository,
                                 StorageService storageService) {
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    public StudentProfileResponse getByUserId(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Профиль ученика не найден"));

        return map(profile);
    }

    /**
     * Partial update: only non-null fields from the request override existing values.
     * This prevents data loss when frontend sends only the fields being edited.
     * avatarKey is NOT writable here — use the avatar upload endpoint.
     */
    public StudentProfileResponse update(Long userId, UpdateStudentProfileRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Профиль ученика не найден"));

        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName().isBlank() ? null : request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName().isBlank() ? null : request.getLastName().trim());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio().isBlank() ? null : request.getBio().trim());
        }
        if (request.getTimezone() != null) {
            profile.setTimezone(request.getTimezone().isBlank() ? null : request.getTimezone().trim());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone().isBlank() ? null : request.getPhone().trim());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity().isBlank() ? null : request.getCity().trim());
        }

        // Update locale on the User entity if provided
        if (request.getPreferredLocale() != null) {
            profile.getUser().setPreferredLocale(LocaleUtils.normalize(request.getPreferredLocale()));
        }

        return map(profile);
    }

    private StudentProfileResponse map(StudentProfile profile) {
        StudentProfileResponse response = new StudentProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setEmail(profile.getUser().getEmail());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setDisplayName(buildDisplayName(profile));

        // Заполнение данных аватара
        response.setAvatarKey(profile.getAvatarKey());
        response.setAvatarUrl(storageService.buildPublicUrl(profile.getAvatarKey()));

        response.setBio(profile.getBio());
        response.setTimezone(profile.getTimezone());
        response.setPhone(profile.getPhone());
        response.setCity(profile.getCity());
        response.setPreferredLocale(profile.getUser().getPreferredLocale());

        // Profile completeness signals for frontend
        List<String> missing = computeMissingFields(profile);
        response.setProfileComplete(missing.isEmpty());
        response.setMissingFields(missing);
        response.setProfileCompletenessPercent(computeCompletenessPercent(missing, STUDENT_TOTAL_FIELDS));
        response.setMemberSince(profile.getCreatedAt() != null ? profile.getCreatedAt().toString() : null);
        return response;
    }

    private String buildDisplayName(StudentProfile profile) {
        String first = safeString(profile.getFirstName());
        String last = safeString(profile.getLastName());
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? profile.getUser().getEmail() : combined;
    }

    private List<String> computeMissingFields(StudentProfile p) {
        List<String> missing = new ArrayList<>();
        if (!hasText(p.getFirstName())) missing.add("firstName");
        if (!hasText(p.getLastName())) missing.add("lastName");
        if (!hasText(p.getBio())) missing.add("bio");
        if (p.getAvatarKey() == null || p.getAvatarKey().isBlank()) missing.add("avatar");
        return missing;
    }

    private int computeCompletenessPercent(List<String> missingFields, int totalFields) {
        if (totalFields <= 0) return 100;
        return (int) Math.round(((double) (totalFields - missingFields.size()) / totalFields) * 100);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }
}