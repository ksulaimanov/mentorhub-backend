package kg.kut.os.mentorhub.mentor.controller;

import kg.kut.os.mentorhub.availability.dto.PublicAvailabilitySlotResponse;
import kg.kut.os.mentorhub.mentor.dto.MentorDirectoryFilter;
import kg.kut.os.mentorhub.mentor.dto.MentorDirectoryItemResponse;
import kg.kut.os.mentorhub.mentor.dto.PublicMentorProfileResponse;
import kg.kut.os.mentorhub.mentor.service.PublicMentorDirectoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/mentors")
public class PublicMentorDirectoryController {

    private final PublicMentorDirectoryService publicMentorDirectoryService;

    public PublicMentorDirectoryController(PublicMentorDirectoryService publicMentorDirectoryService) {
        this.publicMentorDirectoryService = publicMentorDirectoryService;
    }

    @GetMapping
    public ResponseEntity<Page<MentorDirectoryItemResponse>> getDirectory(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean online,
            @RequestParam(required = false) Boolean offline,
            @RequestParam(required = false) Boolean hybrid,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        MentorDirectoryFilter filter = new MentorDirectoryFilter();
        filter.setQuery(query);
        filter.setSpecialization(specialization);
        filter.setCity(city);
        filter.setOnline(online);
        filter.setOffline(offline);
        filter.setHybrid(hybrid);
        filter.setSortBy(sortBy);

        return ResponseEntity.ok(publicMentorDirectoryService.getDirectory(filter, page, size));
    }

    @GetMapping("/{mentorId}")
    public ResponseEntity<PublicMentorProfileResponse> getPublicProfile(@PathVariable Long mentorId) {
        return ResponseEntity.ok(publicMentorDirectoryService.getPublicProfile(mentorId));
    }

    @GetMapping("/{mentorId}/slots")
    public ResponseEntity<List<PublicAvailabilitySlotResponse>> getPublicSlots(@PathVariable Long mentorId) {
        return ResponseEntity.ok(publicMentorDirectoryService.getPublicSlots(mentorId));
    }
}