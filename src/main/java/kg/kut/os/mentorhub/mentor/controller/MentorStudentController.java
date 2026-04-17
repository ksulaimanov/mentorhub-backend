package kg.kut.os.mentorhub.mentor.controller;

import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.common.security.CurrentUser;
import kg.kut.os.mentorhub.mentor.dto.StudentPreviewDto;
import kg.kut.os.mentorhub.mentor.service.MentorStudentInteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentors")
@PreAuthorize("hasRole('MENTOR')")
public class MentorStudentController {

    private final MentorStudentInteractionService interactionService;

    public MentorStudentController(MentorStudentInteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @GetMapping("/student-preview/{studentId}")
    public ResponseEntity<StudentPreviewDto> getStudentPreview(
            @CurrentUser User currentUser,
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(interactionService.getStudentPreview(currentUser.getId(), studentId));
    }
}

