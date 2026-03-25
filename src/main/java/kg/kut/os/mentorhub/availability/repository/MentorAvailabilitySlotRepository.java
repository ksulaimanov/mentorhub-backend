package kg.kut.os.mentorhub.availability.repository;

import jakarta.persistence.LockModeType;
import kg.kut.os.mentorhub.availability.entity.MentorAvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MentorAvailabilitySlotRepository extends JpaRepository<MentorAvailabilitySlot, Long> {
    List<MentorAvailabilitySlot> findAllByMentorUserIdOrderByStartAtAsc(Long userId);
    List<MentorAvailabilitySlot> findAllByMentorIdAndIsActiveTrueOrderByStartAtAsc(Long mentorId);
    Optional<MentorAvailabilitySlot> findByIdAndMentorUserId(Long slotId, Long userId);
    List<MentorAvailabilitySlot> findByMentorIdAndIsActiveTrueAndStartAtAfterOrderByStartAtAsc(
            Long mentorId,
            LocalDateTime startAt
    );

    /** Pessimistic write lock — used during booking creation to prevent overselling. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from MentorAvailabilitySlot s where s.id = :id")
    Optional<MentorAvailabilitySlot> findByIdForUpdate(@Param("id") Long id);
}