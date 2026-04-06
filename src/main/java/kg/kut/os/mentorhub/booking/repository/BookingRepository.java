package kg.kut.os.mentorhub.booking.repository;

import kg.kut.os.mentorhub.booking.entity.Booking;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByAvailabilitySlotId(Long availabilitySlotId);

    List<Booking> findAllByStudentUserIdOrderByStartAtAsc(Long userId);

    List<Booking> findAllByMentorUserIdOrderByStartAtAsc(Long userId);

    Optional<Booking> findByIdAndStudentUserId(Long bookingId, Long userId);

    Optional<Booking> findByIdAndMentorUserId(Long bookingId, Long userId);

    @Query("""
            select b from Booking b
            join fetch b.mentor m
            join fetch m.user mu
            join fetch b.student st
            join fetch st.user su
            join fetch b.availabilitySlot s
            where b.id = :bookingId
              and b.student.user.id = :userId
            """)
    Optional<Booking> findByIdAndStudentUserIdFetched(
            @Param("bookingId") Long bookingId,
            @Param("userId") Long userId
    );

    @Query("""
            select b from Booking b
            join fetch b.mentor m
            join fetch m.user mu
            join fetch b.student st
            join fetch st.user su
            join fetch b.availabilitySlot s
            where b.id = :bookingId
              and b.mentor.user.id = :userId
            """)
    Optional<Booking> findByIdAndMentorUserIdFetched(
            @Param("bookingId") Long bookingId,
            @Param("userId") Long userId
    );

    long countByAvailabilitySlotIdAndStatusIn(Long availabilitySlotId, Collection<BookingStatus> statuses);

    boolean existsByStudentIdAndAvailabilitySlotIdAndStatusIn(
            Long studentId, Long availabilitySlotId, Collection<BookingStatus> statuses
    );

    // ----------------------------------------------------------------
    // Filtered booking lists for student / mentor
    // ----------------------------------------------------------------

    @Query("""
            select b from Booking b
            join fetch b.mentor m
            join fetch m.user mu
            join fetch b.student st
            join fetch st.user su
            join fetch b.availabilitySlot s
            where b.student.user.id = :userId
            order by b.startAt desc
            """)
    List<Booking> findAllByStudentUserIdFetched(@Param("userId") Long userId);

    @Query("""
            select b from Booking b
            join fetch b.mentor m
            join fetch m.user mu
            join fetch b.student st
            join fetch st.user su
            join fetch b.availabilitySlot s
            where b.student.user.id = :userId
              and b.status = :status
            order by b.startAt desc
            """)
    List<Booking> findAllByStudentUserIdAndStatusFetched(
            @Param("userId") Long userId,
            @Param("status") BookingStatus status
    );

    @Query("""
            select b from Booking b
            join fetch b.mentor m
            join fetch m.user mu
            join fetch b.student st
            join fetch st.user su
            join fetch b.availabilitySlot s
            where b.mentor.user.id = :userId
            order by b.startAt desc
            """)
    List<Booking> findAllByMentorUserIdFetched(@Param("userId") Long userId);

    @Query("""
            select b from Booking b
            join fetch b.mentor m
            join fetch m.user mu
            join fetch b.student st
            join fetch st.user su
            join fetch b.availabilitySlot s
            where b.mentor.user.id = :userId
              and b.status = :status
            order by b.startAt desc
            """)
    List<Booking> findAllByMentorUserIdAndStatusFetched(
            @Param("userId") Long userId,
            @Param("status") BookingStatus status
    );

    // ----------------------------------------------------------------
    // Batch booking count per slot (avoids N+1)
    // ----------------------------------------------------------------

    @Query("""
            select b.availabilitySlot.id, count(b) from Booking b
            where b.availabilitySlot.id in :slotIds
              and b.status in :statuses
            group by b.availabilitySlot.id
            """)
    List<Object[]> countBySlotIdsAndStatusIn(
            @Param("slotIds") Collection<Long> slotIds,
            @Param("statuses") Collection<BookingStatus> statuses
    );
    // ----------------------------------------------------------------

    /**
     * Student upcoming events: future bookings with PENDING or CONFIRMED status.
     * Joins mentor and slot eagerly to avoid N+1.
     */
    @Query("""
            select b from Booking b
            join fetch b.mentor m
            join fetch m.user mu
            join fetch b.availabilitySlot s
            where b.student.user.id = :studentUserId
              and b.status in :statuses
              and b.startAt > :now
            order by b.startAt asc
            """)
    List<Booking> findUpcomingByStudentUserId(
            @Param("studentUserId") Long studentUserId,
            @Param("statuses") Collection<BookingStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /**
     * Mentor upcoming events: future bookings on the mentor's slots.
     * Joins student and slot eagerly to avoid N+1.
     */
    @Query("""
            select b from Booking b
            join fetch b.student st
            join fetch st.user su
            join fetch b.availabilitySlot s
            where b.mentor.user.id = :mentorUserId
              and b.status in :statuses
              and b.startAt > :now
            order by b.startAt asc
            """)
    List<Booking> findUpcomingByMentorUserId(
            @Param("mentorUserId") Long mentorUserId,
            @Param("statuses") Collection<BookingStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /**
     * Admin: count upcoming bookings (PENDING + CONFIRMED, startAt in the future).
     */
    @Query("""
            select count(b) from Booking b
            where b.status in :statuses
              and b.startAt > :now
            """)
    long countUpcoming(
            @Param("statuses") Collection<BookingStatus> statuses,
            @Param("now") LocalDateTime now
    );

    /**
     * Admin: count PENDING bookings only (all time).
     */
    long countByStatus(BookingStatus status);

    /**
     * Admin: 10 nearest upcoming bookings with mentor + student joins.
     */
    @Query("""
            select b from Booking b
            join fetch b.mentor m
            join fetch m.user mu
            join fetch b.student st
            join fetch st.user su
            where b.status in :statuses
              and b.startAt > :now
            order by b.startAt asc
            """)
    List<Booking> findTopUpcomingForAdmin(
            @Param("statuses") Collection<BookingStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // ----------------------------------------------------------------
    // Dashboard stats counters
    // ----------------------------------------------------------------

    /** Total bookings for a student (all statuses). */
    long countByStudentUserId(Long studentUserId);

    /** Bookings for a student filtered by status (e.g. COMPLETED). */
    long countByStudentUserIdAndStatus(Long studentUserId, BookingStatus status);

    /** Total bookings for a mentor (all statuses). */
    long countByMentorUserId(Long mentorUserId);

    /** Bookings for a mentor filtered by status (e.g. COMPLETED). */
    long countByMentorUserIdAndStatus(Long mentorUserId, BookingStatus status);

    /** Distinct students who have at least one booking with this mentor. */
    @Query("""
            select count(distinct b.student.id) from Booking b
            where b.mentor.user.id = :mentorUserId
            """)
    long countDistinctStudentsByMentorUserId(@Param("mentorUserId") Long mentorUserId);
}