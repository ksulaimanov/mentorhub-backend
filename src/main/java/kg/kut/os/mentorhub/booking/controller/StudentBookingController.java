package kg.kut.os.mentorhub.booking.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.booking.dto.BookingResponse;
import kg.kut.os.mentorhub.booking.dto.CreateBookingRequest;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import kg.kut.os.mentorhub.booking.service.BookingService;
import kg.kut.os.mentorhub.common.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/bookings")
public class StudentBookingController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    public StudentBookingController(BookingService bookingService, CurrentUserService currentUserService) {
        this.bookingService = bookingService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(currentUserService.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getStudentBookings(
            @RequestParam(required = false) BookingStatus status
    ) {
        return ResponseEntity.ok(bookingService.getStudentBookings(currentUserService.getCurrentUserId(), status));
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(
                bookingService.cancelByStudent(currentUserService.getCurrentUserId(), bookingId)
        );
    }
}