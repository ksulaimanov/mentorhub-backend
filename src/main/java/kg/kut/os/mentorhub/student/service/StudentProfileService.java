package kg.kut.os.mentorhub.student.service;

import kg.kut.os.mentorhub.common.exception.BadRequestException;
import kg.kut.os.mentorhub.student.dto.StudentProfileResponse;
import kg.kut.os.mentorhub.student.dto.UpdateStudentProfileRequest;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;

    public StudentProfileService(StudentProfileRepository studentProfileRepository) {
        this.studentProfileRepository = studentProfileRepository;
    }

    public StudentProfileResponse getByUserId(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Профиль ученика не найден"));

        return map(profile);
    }

    public StudentProfileResponse update(Long userId, UpdateStudentProfileRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Профиль ученика не найден"));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setAvatarKey(request.getAvatarKey());
        profile.setBio(request.getBio());
        profile.setTimezone(request.getTimezone());
        profile.setPhone(request.getPhone());
        profile.setCity(request.getCity());

        return map(profile);
    }

    private StudentProfileResponse map(StudentProfile profile) {
        StudentProfileResponse response = new StudentProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setEmail(profile.getUser().getEmail());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setAvatarKey(profile.getAvatarKey());
        response.setBio(profile.getBio());
        response.setTimezone(profile.getTimezone());
        response.setPhone(profile.getPhone());
        response.setCity(profile.getCity());
        return response;
    }
}