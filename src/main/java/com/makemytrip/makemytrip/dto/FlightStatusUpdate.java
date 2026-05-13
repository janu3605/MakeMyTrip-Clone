package com.makemytrip.makemytrip.dto;

public class FlightStatusUpdate {

    private String flightId;
    private String status; // ON_TIME, DELAYED, BOARDING, IN_AIR, LANDED
    private String message;
    private String reason; // E.g., Weather, Runway Access
    private int progress;
    private String lastUpdated;

    public FlightStatusUpdate() {
    }

    public FlightStatusUpdate(String flightId, String status, String message, String reason, int progress) {
        this.flightId = flightId;
        this.status = status;
        this.message = message;
        this.reason = reason;
        this.progress = progress;
        this.lastUpdated = java.time.LocalTime.now().toString();
    }

    // Getters and Setters
    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
