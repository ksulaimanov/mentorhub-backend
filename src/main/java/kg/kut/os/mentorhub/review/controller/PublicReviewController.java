package kg.kut.os.mentorhub.review.controller;

import kg.kut.os.mentorhub.review.dto.ReviewResponse;
import kg.kut.os.mentorhub.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/mentors/{mentorId}/reviews")
public class PublicReviewController {

    private final ReviewService reviewService;

    public PublicReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getMentorReviews(@PathVariable Long mentorId) {
        return ResponseEntity.ok(reviewService.getPublicMentorReviews(mentorId));
    }
}