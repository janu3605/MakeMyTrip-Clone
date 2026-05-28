package com.makemytrip.makemytrip.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.makemytrip.makemytrip.dto.RecommendationFeedbackRequest;
import com.makemytrip.makemytrip.dto.RecommendationResponse;
import com.makemytrip.makemytrip.models.Recommendation;
import com.makemytrip.makemytrip.services.RecommendationService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * Get personalized recommendations for a user.
     * Optional type filter: HOTEL or FLIGHT.
     */
    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
            @RequestParam String userId,
            @RequestParam(required = false) String type) {
        List<RecommendationResponse> recs = recommendationService.getRecommendations(userId, type);
        return ResponseEntity.ok(recs);
    }

    /**
     * Get trending recommendations (no auth required).
     */
    @GetMapping("/trending")
    public ResponseEntity<List<RecommendationResponse>> getTrendingRecommendations() {
        List<RecommendationResponse> recs = recommendationService.getTrendingRecommendations();
        return ResponseEntity.ok(recs);
    }

    /**
     * Submit feedback on a recommendation (HELPFUL or IRRELEVANT).
     */
    @PostMapping("/{id}/feedback")
    public ResponseEntity<Recommendation> submitFeedback(
            @PathVariable String id,
            @RequestBody RecommendationFeedbackRequest request) {
        Recommendation rec = recommendationService.submitFeedback(id, request.getUserId(), request.getFeedback());
        return ResponseEntity.ok(rec);
    }
}
