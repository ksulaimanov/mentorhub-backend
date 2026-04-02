package kg.kut.os.mentorhub.availability.repository;

import jakarta.persistence.LockModeType;
import kg.kut.os.mentorhub.availability.entity.MentorAvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
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

    /** Count future active slots for a mentor — used for hasAvailableSlots flag. */
    @Query("select count(s) from MentorAvailabilitySlot s where s.mentor.id = :mentorId and s.isActive = true and s.startAt > :now")
    long countFutureActiveSlots(@Param("mentorId") Long mentorId, @Param("now") LocalDateTime now);

    /** Batch count of future active slots per mentor — avoids N+1 in directory listing. Returns rows of [mentorId, count]. */
    @Query("select s.mentor.id, count(s) from MentorAvailabilitySlot s where s.mentor.id in :mentorIds and s.isActive = true and s.startAt > :now group by s.mentor.id")
    List<Object[]> countFutureActiveSlotsByMentorIds(@Param("mentorIds") Collection<Long> mentorIds, @Param("now") LocalDateTime now);

    /** Pessimistic write lock — used during booking creation to prevent overselling. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from MentorAvailabilitySlot s where s.id = :id")
    Optional<MentorAvailabilitySlot> findByIdForUpdate(@Param("id") Long id);

    /**
     * Check for overlapping active slots for the same mentor.
     * Two slots overlap when: existingStart < newEnd AND existingEnd > newStart.
     * Optionally excludes a specific slot (for updates).
     */
    @Query("""
            select count(s) > 0 from MentorAvailabilitySlot s
            where s.mentor.id = :mentorId
              and s.isActive = true
              and s.startAt < :endAt
              and s.endAt > :startAt
              and (:excludeSlotId is null or s.id <> :excludeSlotId)
            """)
    boolean existsOverlapping(
            @Param("mentorId") Long mentorId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludeSlotId") Long excludeSlotId
    );
}