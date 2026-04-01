package kg.kut.os.mentorhub.booking.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.booking.dto.BookingResponse;
import kg.kut.os.mentorhub.booking.dto.UpdateBookingStatusRequest;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import kg.kut.os.mentorhub.booking.service.BookingService;
import kg.kut.os.mentorhub.common.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentor/bookings")
public class MentorBookingController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    public MentorBookingController(BookingService bookingService, CurrentUserService currentUserService) {
        this.bookingService = bookingService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getMentorBookings(
            @RequestParam(required = false) BookingStatus status
    ) {
        return ResponseEntity.ok(bookingService.getMentorBookings(currentUserService.getCurrentUserId(), status));
    }

    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable Long bookingId,
            @Valid @RequestBody UpdateBookingStatusRequest request
    ) {
        return ResponseEntity.ok(
                bookingService.updateMentorBookingStatus(currentUserService.getCurrentUserId(), bookingId, request)
        );
    }
}