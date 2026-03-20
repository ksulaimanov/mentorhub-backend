package kg.kut.os.mentorhub.availability.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.availability.dto.AvailabilitySlotResponse;
import kg.kut.os.mentorhub.availability.dto.CreateAvailabilitySlotRequest;
import kg.kut.os.mentorhub.availability.dto.UpdateAvailabilitySlotRequest;
import kg.kut.os.mentorhub.availability.service.MentorAvailabilitySlotService;
import kg.kut.os.mentorhub.common.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentor/availability-slots")
public class MentorAvailabilitySlotController {

    private final MentorAvailabilitySlotService mentorAvailabilitySlotService;
    private final CurrentUserService currentUserService;

    public MentorAvailabilitySlotController(
            MentorAvailabilitySlotService mentorAvailabilitySlotService,
            CurrentUserService currentUserService
    ) {
        this.mentorAvailabilitySlotService = mentorAvailabilitySlotService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<AvailabilitySlotResponse> create(@Valid @RequestBody CreateAvailabilitySlotRequest request) {
        return ResponseEntity.ok(mentorAvailabilitySlotService.create(currentUserService.getCurrentUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<AvailabilitySlotResponse>> getMentorSlots() {
        return ResponseEntity.ok(mentorAvailabilitySlotService.getMentorSlots(currentUserService.getCurrentUserId()));
    }

    @PutMapping("/{slotId}")
    public ResponseEntity<AvailabilitySlotResponse> update(
            @PathVariable Long slotId,
            @Valid @RequestBody UpdateAvailabilitySlotRequest request
    ) {
        return ResponseEntity.ok(
                mentorAvailabilitySlotService.update(currentUserService.getCurrentUserId(), slotId, request)
        );
    }

    @PatchMapping("/{slotId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long slotId) {
        mentorAvailabilitySlotService.deactivate(currentUserService.getCurrentUserId(), slotId);
        return ResponseEntity.noContent().build();
    }
}