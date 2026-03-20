package kg.kut.os.mentorhub.booking.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.booking.dto.BookingResponse;
import kg.kut.os.mentorhub.booking.dto.CreateBookingRequest;
import kg.kut.os.mentorhub.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/{userId}/bookings")
public class StudentBookingController {

    private final BookingService bookingService;

    public StudentBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable Long userId,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return ResponseEntity.ok(bookingService.createBooking(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getStudentBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getStudentBookings(userId));
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long userId,
            @PathVariable Long bookingId
    ) {
        bookingService.cancelByStudent(userId, bookingId);
        return ResponseEntity.noContent().build();
    }
}