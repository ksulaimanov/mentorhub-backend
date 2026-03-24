package kg.kut.os.mentorhub.mentor.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.mentor.dto.MentorProfileResponse;
import kg.kut.os.mentorhub.mentor.dto.UpdateMentorProfileRequest;
import kg.kut.os.mentorhub.mentor.service.MentorProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/mentor")
public class MentorProfileController {

    private final MentorProfileService mentorProfileService;

    public MentorProfileController(MentorProfileService mentorProfileService) {
        this.mentorProfileService = mentorProfileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<MentorProfileResponse> getMyProfile(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(mentorProfileService.getByEmail(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<MentorProfileResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody UpdateMentorProfileRequest request
    ) {
        String email = principal.getName();
        return ResponseEntity.ok(mentorProfileService.updateByEmail(email, request));
    }
}