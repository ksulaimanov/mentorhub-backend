package kg.kut.os.mentorhub.student.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.student.dto.StudentProfileResponse;
import kg.kut.os.mentorhub.student.dto.UpdateStudentProfileRequest;
import kg.kut.os.mentorhub.student.service.StudentProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    public StudentProfileController(StudentProfileService studentProfileService) {
        this.studentProfileService = studentProfileService;
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<StudentProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(studentProfileService.getByUserId(userId));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<StudentProfileResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateStudentProfileRequest request
    ) {
        return ResponseEntity.ok(studentProfileService.update(userId, request));
    }
}