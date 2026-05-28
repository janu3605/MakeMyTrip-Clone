package com.makemytrip.makemytrip.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.makemytrip.makemytrip.dto.ReviewFlagRequest;
import com.makemytrip.makemytrip.dto.ReviewReplyRequest;
import com.makemytrip.makemytrip.dto.ReviewRequest;
import com.makemytrip.makemytrip.models.Review;
import com.makemytrip.makemytrip.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("")
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        try {
            Review review = reviewService.createReview(request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("")
    public ResponseEntity<List<Review>> getReviews(
            @RequestParam String entityId,
            @RequestParam String entityType,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {
        List<Review> reviews = reviewService.getReviewsForEntity(entityId, entityType, sortBy);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/rating")
    public ResponseEntity<Map<String, Object>> getRatingSummary(
            @RequestParam String entityId,
            @RequestParam String entityType) {
        Map<String, Object> summary = reviewService.getAverageRating(entityId, entityType);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<?> addReply(
            @PathVariable String reviewId,
            @RequestBody ReviewReplyRequest request) {
        try {
            Review review = reviewService.addReply(reviewId, request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<?> voteHelpful(
            @PathVariable String reviewId,
            @RequestParam String userId) {
        try {
            Review review = reviewService.voteHelpful(reviewId, userId);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{reviewId}/flag")
    public ResponseEntity<?> flagReview(
            @PathVariable String reviewId,
            @RequestBody ReviewFlagRequest request) {
        try {
            Review review = reviewService.flagReview(reviewId, request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<Review>> getUserReviews(@RequestParam String userId) {
        List<Review> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<Review>> getFlaggedReviews() {
        List<Review> reviews = reviewService.getFlaggedReviews();
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{reviewId}/moderate")
    public ResponseEntity<?> moderateReview(
            @PathVariable String reviewId,
            @RequestParam String action) {
        try {
            Review review = reviewService.moderateReview(reviewId, action);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
