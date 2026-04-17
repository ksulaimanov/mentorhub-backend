package kg.kut.os.mentorhub.review.repository;

import kg.kut.os.mentorhub.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBookingId(Long bookingId);

    List<Review> findAllByMentorIdOrderByCreatedAtDesc(Long mentorId);

    Page<Review> findAllByMentorIdOrderByCreatedAtDesc(Long mentorId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE (:lowRatingOnly = false OR r.rating <= 2) ORDER BY r.createdAt DESC")
    Page<Review> findAllReviewsForAdmin(@Param("lowRatingOnly") boolean lowRatingOnly, Pageable pageable);

    @Query("select avg(r.rating) from Review r where r.mentor.id = :mentorId")
    BigDecimal findAverageRatingByMentorId(Long mentorId);

    @Query("select count(r) from Review r where r.mentor.id = :mentorId")
    long countByMentorId(Long mentorId);

    Optional<Review> findByIdAndStudentUserId(Long reviewId, Long userId);

    /**
     * Batch review count per mentor — avoids N+1 in directory listing.
     * Returns rows of [mentorId, count].
     */
    @Query("select r.mentor.id, count(r) from Review r where r.mentor.id in :mentorIds group by r.mentor.id")
    List<Object[]> countByMentorIds(@Param("mentorIds") Collection<Long> mentorIds);

    /** Global average rating across all mentors — for admin dashboard. */
    @Query("select avg(r.rating) from Review r")
    BigDecimal findGlobalAverageRating();
}