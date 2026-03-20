package kg.kut.os.mentorhub.booking.repository;

import kg.kut.os.mentorhub.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByAvailabilitySlotId(Long availabilitySlotId);
    List<Booking> findAllByStudentUserIdOrderByStartAtAsc(Long userId);
    List<Booking> findAllByMentorUserIdOrderByStartAtAsc(Long userId);
    Optional<Booking> findByIdAndStudentUserId(Long bookingId, Long userId);
    Optional<Booking> findByIdAndMentorUserId(Long bookingId, Long userId);
}