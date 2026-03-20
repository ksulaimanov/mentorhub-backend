package kg.kut.os.mentorhub.mentor.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.mentor.dto.MentorProfileResponse;
import kg.kut.os.mentorhub.mentor.dto.UpdateMentorProfileRequest;
import kg.kut.os.mentorhub.mentor.service.MentorProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentors")
public class MentorProfileController {

    private final MentorProfileService mentorProfileService;

    public MentorProfileController(MentorProfileService mentorProfileService) {
        this.mentorProfileService = mentorProfileService;
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<MentorProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(mentorProfileService.getByUserId(userId));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<MentorProfileResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMentorProfileRequest request
    ) {
        return ResponseEntity.ok(mentorProfileService.update(userId, request));
    }
}