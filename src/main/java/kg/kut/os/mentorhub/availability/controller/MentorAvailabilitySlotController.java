package kg.kut.os.mentorhub.availability.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.availability.dto.AvailabilitySlotResponse;
import kg.kut.os.mentorhub.availability.dto.CreateAvailabilitySlotRequest;
import kg.kut.os.mentorhub.availability.dto.UpdateAvailabilitySlotRequest;
import kg.kut.os.mentorhub.availability.service.MentorAvailabilitySlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentors/{userId}/availability-slots")
public class MentorAvailabilitySlotController {

    private final MentorAvailabilitySlotService mentorAvailabilitySlotService;

    public MentorAvailabilitySlotController(MentorAvailabilitySlotService mentorAvailabilitySlotService) {
        this.mentorAvailabilitySlotService = mentorAvailabilitySlotService;
    }

    @PostMapping
    public ResponseEntity<AvailabilitySlotResponse> create(
            @PathVariable Long userId,
            @Valid @RequestBody CreateAvailabilitySlotRequest request
    ) {
        return ResponseEntity.ok(mentorAvailabilitySlotService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<AvailabilitySlotResponse>> getMentorSlots(@PathVariable Long userId) {
        return ResponseEntity.ok(mentorAvailabilitySlotService.getMentorSlots(userId));
    }

    @PutMapping("/{slotId}")
    public ResponseEntity<AvailabilitySlotResponse> update(
            @PathVariable Long userId,
            @PathVariable Long slotId,
            @Valid @RequestBody UpdateAvailabilitySlotRequest request
    ) {
        return ResponseEntity.ok(mentorAvailabilitySlotService.update(userId, slotId, request));
    }

    @PatchMapping("/{slotId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long userId,
            @PathVariable Long slotId
    ) {
        mentorAvailabilitySlotService.deactivate(userId, slotId);
        return ResponseEntity.noContent().build();
    }
}