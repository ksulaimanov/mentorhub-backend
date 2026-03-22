package kg.kut.os.mentorhub.review.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.common.security.CurrentUserService;
import kg.kut.os.mentorhub.review.dto.CreateReviewRequest;
import kg.kut.os.mentorhub.review.dto.ReviewResponse;
import kg.kut.os.mentorhub.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/reviews")
public class StudentReviewController {

    private final ReviewService reviewService;
    private final CurrentUserService currentUserService;

    public StudentReviewController(ReviewService reviewService, CurrentUserService currentUserService) {
        this.reviewService = reviewService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.createReview(currentUserService.getCurrentUserId(), request));
    }
}