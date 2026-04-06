package kg.kut.os.mentorhub.mentor.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.common.security.CurrentUserService;
import kg.kut.os.mentorhub.mentor.dto.MentorProfileResponse;
import kg.kut.os.mentorhub.mentor.dto.UpdateMentorProfileRequest;
import kg.kut.os.mentorhub.mentor.service.MentorProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentor")
public class MentorProfileController {

    private final MentorProfileService mentorProfileService;
    private final CurrentUserService currentUserService;

    public MentorProfileController(MentorProfileService mentorProfileService,
                                   CurrentUserService currentUserService) {
        this.mentorProfileService = mentorProfileService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/profile")
    public ResponseEntity<MentorProfileResponse> getMyProfile() {
        return ResponseEntity.ok(mentorProfileService.getByUserId(currentUserService.getCurrentUserId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<MentorProfileResponse> updateProfile(
            @Valid @RequestBody UpdateMentorProfileRequest request
    ) {
        return ResponseEntity.ok(mentorProfileService.update(currentUserService.getCurrentUserId(), request));
    }
}