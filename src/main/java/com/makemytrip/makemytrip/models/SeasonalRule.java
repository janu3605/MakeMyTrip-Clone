package com.makemytrip.makemytrip.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "seasonal_rules")
public class SeasonalRule {

    @Id
    private String _id;
    private String name;        // "Diwali Peak", "Summer Holidays"
    private String startDate;   // "2026-10-20"
    private String endDate;     // "2026-11-05"
    private double multiplier;  // 1.20 = 20% increase
    private boolean active;

    public SeasonalRule() {
    }

    public SeasonalRule(String name, String startDate, String endDate, double multiplier, boolean active) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.multiplier = multiplier;
        this.active = active;
    }

    // Getters and Setters
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
