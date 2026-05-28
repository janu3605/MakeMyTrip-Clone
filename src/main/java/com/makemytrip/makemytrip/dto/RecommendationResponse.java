package com.makemytrip.makemytrip.dto;

public class RecommendationResponse {

    private String id;
    private String entityId;
    private String entityType;
    private double score;
    private String reason;
    private String reasonCode;
    private String feedback;

    // Enriched entity info
    private String entityName;
    private String entityLocation;
    private double entityPrice;
    private String entityImageUrl;
    private String entityCategory;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
