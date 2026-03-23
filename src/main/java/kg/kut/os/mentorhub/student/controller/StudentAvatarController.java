package kg.kut.os.mentorhub.student.controller;

import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.common.dto.AvatarResponse;
import kg.kut.os.mentorhub.common.security.CurrentUser;
import kg.kut.os.mentorhub.student.service.StudentAvatarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/student/profile/avatar")
public class StudentAvatarController {

    private final StudentAvatarService studentAvatarService;

    public StudentAvatarController(StudentAvatarService studentAvatarService) {
        this.studentAvatarService = studentAvatarService;
    }

    @PostMapping
    public ResponseEntity<AvatarResponse> uploadAvatar(
            @CurrentUser User currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(studentAvatarService.uploadAvatar(currentUser, file));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAvatar(@CurrentUser User currentUser) {
        studentAvatarService.deleteAvatar(currentUser);
        return ResponseEntity.noContent().build();
    }
}