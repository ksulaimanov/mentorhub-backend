package kg.kut.os.mentorhub.student.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.common.security.CurrentUserService;
import kg.kut.os.mentorhub.student.dto.StudentProfileResponse;
import kg.kut.os.mentorhub.student.dto.UpdateStudentProfileRequest;
import kg.kut.os.mentorhub.student.service.StudentProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;
    private final CurrentUserService currentUserService;

    public StudentProfileController(StudentProfileService studentProfileService, CurrentUserService currentUserService) {
        this.studentProfileService = studentProfileService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<StudentProfileResponse> getProfile() {
        return ResponseEntity.ok(studentProfileService.getByUserId(currentUserService.getCurrentUserId()));
    }

    @PutMapping
    public ResponseEntity<StudentProfileResponse> updateProfile(@Valid @RequestBody UpdateStudentProfileRequest request) {
        return ResponseEntity.ok(studentProfileService.update(currentUserService.getCurrentUserId(), request));
    }
}