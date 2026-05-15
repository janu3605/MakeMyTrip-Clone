package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "price_freezes")
public class PriceFreeze {

    @Id
    private String _id;
    private String userId;
    private String entityId;      // Flight or Hotel ID
    private String entityType;    // "FLIGHT" or "HOTEL"
    private double frozenPrice;   // The price locked at freeze time
    private double freezeFee;     // Fee charged: ₹99 (1 ticket), ₹149 (2-3), ₹299 (4+)
    private int quantity;         // Number of tickets/rooms being frozen
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // createdAt + 24 hours
    private boolean active;

    public PriceFreeze() {
    }

    public PriceFreeze(String userId, String entityId, String entityType,
                       double frozenPrice, double freezeFee, int quantity) {
        this.userId = userId;
        this.entityId = entityId;
        this.entityType = entityType;
        this.frozenPrice = frozenPrice;
        this.freezeFee = freezeFee;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusHours(24);
        this.active = true;
    }

    // Getters and Setters
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
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

    public double getFrozenPrice() {
        return frozenPrice;
    }

    public void setFrozenPrice(double frozenPrice) {
        this.frozenPrice = frozenPrice;
    }

    public double getFreezeFee() {
        return freezeFee;
    }

    public void setFreezeFee(double freezeFee) {
        this.freezeFee = freezeFee;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
