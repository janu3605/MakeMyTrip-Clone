package com.makemytrip.makemytrip.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "recommendations")
public class Recommendation {

    @Id
    private String id;
    private String userId;
    private String entityId;
    private String entityType; // HOTEL, FLIGHT, DESTINATION
    private double score;      // 0-100 relevance score
    private String reason;     // Human-readable: "You liked beaches! Try Bali."
    private String reasonCode; // REPEAT_DESTINATION, SIMILAR_CATEGORY, COLLABORATIVE, TRENDING, PRICE_RANGE, REVIEW_BASED
    private String feedback;   // HELPFUL, IRRELEVANT, or null
    private Instant feedbackDate;
    private Instant createdAt;
    private boolean viewed;

    // Extra fields for enriched display
    private String entityName;
    private String entityLocation;
    private double entityPrice;
    private String entityImageUrl;
    private String entityCategory; // beach, mountain, city, heritage

    public Recommendation() {
        this.createdAt = Instant.now();
        this.viewed = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Instant getFeedbackDate() {
        return feedbackDate;
    }

    public void setFeedbackDate(Instant feedbackDate) {
        this.feedbackDate = feedbackDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityLocation() {
        return entityLocation;
    }

    public void setEntityLocation(String entityLocation) {
        this.entityLocation = entityLocation;
    }

    public double getEntityPrice() {
        return entityPrice;
    }

    public void setEntityPrice(double entityPrice) {
        this.entityPrice = entityPrice;
    }

    public String getEntityImageUrl() {
        return entityImageUrl;
    }

    public void setEntityImageUrl(String entityImageUrl) {
        this.entityImageUrl = entityImageUrl;
    }

    public String getEntityCategory() {
        return entityCategory;
    }

    public void setEntityCategory(String entityCategory) {
        this.entityCategory = entityCategory;
    }
}
