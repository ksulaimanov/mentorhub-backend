package kg.kut.os.mentorhub.admin.controller;

import kg.kut.os.mentorhub.auth.entity.RoleCode;
import kg.kut.os.mentorhub.auth.repository.UserRepository;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import kg.kut.os.mentorhub.booking.repository.BookingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public AdminController(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getShortStats() {
        long totalMentors = userRepository.countByRoleCode(RoleCode.ROLE_MENTOR);
        long totalStudents = userRepository.countByRoleCode(RoleCode.ROLE_STUDENT);
        long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);

        return ResponseEntity.ok(Map.of(
                "totalMentors", totalMentors,
                "totalStudents", totalStudents,
                "successfulBookings", completedBookings
        ));
    }
}

