package com.makemytrip.makemytrip.dto;

public class RecommendationFeedbackRequest {

    private String userId;
    private String feedback; // HELPFUL or IRRELEVANT

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
