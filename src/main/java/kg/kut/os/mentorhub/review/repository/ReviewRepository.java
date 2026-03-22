package kg.kut.os.mentorhub.review.repository;

import kg.kut.os.mentorhub.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBookingId(Long bookingId);

    List<Review> findAllByMentorIdOrderByCreatedAtDesc(Long mentorId);

    @Query("select avg(r.rating) from Review r where r.mentor.id = :mentorId")
    BigDecimal findAverageRatingByMentorId(Long mentorId);

    @Query("select count(r) from Review r where r.mentor.id = :mentorId")
    long countByMentorId(Long mentorId);

    Optional<Review> findByIdAndStudentUserId(Long reviewId, Long userId);
}