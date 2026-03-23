package kg.kut.os.mentorhub.mentor.controller;

import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.common.dto.AvatarResponse;
import kg.kut.os.mentorhub.common.security.CurrentUser;
import kg.kut.os.mentorhub.mentor.service.MentorAvatarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mentor/profile/avatar")
public class MentorAvatarController {

    private final MentorAvatarService mentorAvatarService;

    public MentorAvatarController(MentorAvatarService mentorAvatarService) {
        this.mentorAvatarService = mentorAvatarService;
    }

    @PostMapping
    public ResponseEntity<AvatarResponse> uploadAvatar(
            @CurrentUser User currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(mentorAvatarService.uploadAvatar(currentUser, file));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAvatar(@CurrentUser User currentUser) {
        mentorAvatarService.deleteAvatar(currentUser);
        return ResponseEntity.noContent().build();
    }
}