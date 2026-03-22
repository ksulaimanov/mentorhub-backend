package kg.kut.os.mentorhub.review.service;

import kg.kut.os.mentorhub.booking.entity.Booking;
import kg.kut.os.mentorhub.booking.entity.BookingStatus;
import kg.kut.os.mentorhub.booking.repository.BookingRepository;
import kg.kut.os.mentorhub.common.exception.BadRequestException;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import kg.kut.os.mentorhub.review.dto.CreateReviewRequest;
import kg.kut.os.mentorhub.review.dto.ReviewResponse;
import kg.kut.os.mentorhub.review.entity.Review;
import kg.kut.os.mentorhub.review.repository.ReviewRepository;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final MentorProfileRepository mentorProfileRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            MentorProfileRepository mentorProfileRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.mentorProfileRepository = mentorProfileRepository;
    }

    public ReviewResponse createReview(Long studentUserId, CreateReviewRequest request) {
        Booking booking = bookingRepository.findByIdAndStudentUserId(request.getBookingId(), studentUserId)
                .orElseThrow(() -> new BadRequestException("Бронирование не найдено"));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Отзыв можно оставить только после завершённого занятия");
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new BadRequestException("Отзыв по этому занятию уже оставлен");
        }

        StudentProfile student = booking.getStudent();
        MentorProfile mentor = booking.getMentor();

        Review review = new Review();
        review.setBooking(booking);
        review.setStudent(student);
        review.setMentor(mentor);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        recalculateMentorStats(mentor.getId());

        return map(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getPublicMentorReviews(Long mentorId) {
        return reviewRepository.findAllByMentorIdOrderByCreatedAtDesc(mentorId)
                .stream()
                .map(this::map)
                .toList();
    }

    private void recalculateMentorStats(Long mentorId) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new BadRequestException("Профиль ментора не найден"));

        BigDecimal average = reviewRepository.findAverageRatingByMentorId(mentorId);
        long completedLessons = bookingRepository.findAllByMentorUserIdOrderByStartAtAsc(mentor.getUser().getId())
                .stream()
                .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
                .count();

        mentor.setAverageRating(
                average == null ? BigDecimal.ZERO : average.setScale(2, RoundingMode.HALF_UP)
        );
        mentor.setLessonsCompleted((int) completedLessons);
    }

    private ReviewResponse map(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setBookingId(review.getBooking().getId());
        response.setMentorId(review.getMentor().getId());
        response.setStudentId(review.getStudent().getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}