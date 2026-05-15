package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "price_history")
public class PriceHistory {

    @Id
    private String _id;
    private String entityId;      // Flight or Hotel ID
    private String entityType;    // "FLIGHT" or "HOTEL"
    private double oldPrice;
    private double newPrice;
    private double multiplier;    // The combined multiplier that was applied
    private String reason;        // "DEMAND_SURGE", "SEASONAL", "LOW_AVAILABILITY", etc.
    private LocalDateTime timestamp;

    public PriceHistory() {
    }

    public PriceHistory(String entityId, String entityType, double oldPrice, double newPrice,
                        double multiplier, String reason) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.multiplier = multiplier;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
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

    public double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
