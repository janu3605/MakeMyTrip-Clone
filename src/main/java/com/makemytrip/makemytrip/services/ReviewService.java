package com.makemytrip.makemytrip.services;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.makemytrip.makemytrip.dto.ReviewFlagRequest;
import com.makemytrip.makemytrip.dto.ReviewReplyRequest;
import com.makemytrip.makemytrip.dto.ReviewRequest;
import com.makemytrip.makemytrip.models.Review;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.repositories.ReviewRepository;
import com.makemytrip.makemytrip.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    public Review createReview(ReviewRequest request) {
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }


        if (!"FLIGHT".equals(request.getEntityType()) && !"HOTEL".equals(request.getEntityType())) {
            throw new RuntimeException("Entity type must be FLIGHT or HOTEL");
        }


        Optional<Users> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }


        if (reviewRepository.existsByUserIdAndEntityIdAndEntityType(
                request.getUserId(), request.getEntityId(), request.getEntityType())) {
            throw new RuntimeException("You have already reviewed this entity");
        }

        Users user = userOpt.get();
        String now = Instant.now().toString();

        Review review = new Review();
        review.setUserId(request.getUserId());
        review.setUserName(user.getFirstName());
        review.setEntityId(request.getEntityId());
        review.setEntityType(request.getEntityType());
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        if (request.getPhotos() != null) {
            review.setPhotos(request.getPhotos());
        }
        review.setModerationStatus("APPROVED");
        review.setCreatedAt(now);
        review.setUpdatedAt(now);

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForEntity(String entityId, String entityType, String sortBy) {
        List<Review> reviews = reviewRepository.findByEntityIdAndEntityTypeAndModerationStatus(
                entityId, entityType, "APPROVED");

        Comparator<Review> comparator;
        switch (sortBy != null ? sortBy : "newest") {
            case "oldest":
                comparator = Comparator.comparing(Review::getCreatedAt);
                break;
            case "highest":
                comparator = Comparator.comparingInt(Review::getRating).reversed();
                break;
            case "lowest":
                comparator = Comparator.comparingInt(Review::getRating);
                break;
            case "most_helpful":
                comparator = Comparator.comparingInt(Review::getHelpfulCount).reversed();
                break;
            case "newest":
            default:
                comparator = Comparator.comparing(Review::getCreatedAt).reversed();
                break;
        }

        reviews.sort(comparator);
        return reviews;
    }

    public Review addReply(String reviewId, ReviewReplyRequest request) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            throw new RuntimeException("Review not found");
        }

        Optional<Users> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Review review = reviewOpt.get();
        Users user = userOpt.get();

        Review.Reply reply = new Review.Reply();
        reply.setUserId(request.getUserId());
        reply.setUserName(user.getFirstName());
        reply.setContent(request.getContent());
        reply.setCreatedAt(Instant.now().toString());

        review.getReplies().add(reply);
        return reviewRepository.save(review);
    }

    public Review voteHelpful(String reviewId, String userId) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            throw new RuntimeException("Review not found");
        }

        Review review = reviewOpt.get();

        if (review.getHelpfulVoters().contains(userId)) {
            throw new RuntimeException("Already voted");
        }

        review.getHelpfulVoters().add(userId);
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        return reviewRepository.save(review);
    }

    public Review flagReview(String reviewId, ReviewFlagRequest request) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            throw new RuntimeException("Review not found");
        }

        Review review = reviewOpt.get();
        review.setFlagged(true);
        review.setFlagReason(request.getReason());
        review.setFlaggedBy(request.getUserId());
        review.setModerationStatus("PENDING");
        review.setUpdatedAt(Instant.now().toString());

        return reviewRepository.save(review);
    }

    public List<Review> getFlaggedReviews() {
        return reviewRepository.findByFlaggedTrue();
    }

    public Review moderateReview(String reviewId, String action) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            throw new RuntimeException("Review not found");
        }

        Review review = reviewOpt.get();

        if ("APPROVED".equals(action)) {
            review.setFlagged(false);
            review.setFlagReason(null);
            review.setFlaggedBy(null);
            review.setModerationStatus("APPROVED");
        } else if ("REJECTED".equals(action)) {
            review.setModerationStatus("REJECTED");
        } else {
            throw new RuntimeException("Invalid action. Must be APPROVED or REJECTED");
        }

        review.setUpdatedAt(Instant.now().toString());
        return reviewRepository.save(review);
    }

    public Map<String, Object> getAverageRating(String entityId, String entityType) {
        List<Review> reviews = reviewRepository.findByEntityIdAndEntityTypeAndModerationStatus(
                entityId, entityType, "APPROVED");

        Map<String, Object> result = new HashMap<>();
        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0);
        }

        if (reviews.isEmpty()) {
            result.put("averageRating", 0.0);
            result.put("totalReviews", 0);
            result.put("ratingDistribution", ratingDistribution);
            return result;
        }

        double sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
            ratingDistribution.put(review.getRating(),
                    ratingDistribution.get(review.getRating()) + 1);
        }

        double average = Math.round((sum / reviews.size()) * 10.0) / 10.0;

        result.put("averageRating", average);
        result.put("totalReviews", reviews.size());
        result.put("ratingDistribution", ratingDistribution);
        return result;
    }

    public List<Review> getUserReviews(String userId) {
        return reviewRepository.findByUserId(userId);
    }
}
