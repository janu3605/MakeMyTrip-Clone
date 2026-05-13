package com.makemytrip.makemytrip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveFlight {

    private String flightId;
    private String route; // e.g., "DEL -> BOM"
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String status; // SCHEDULED, BOARDING, IN_AIR, LANDED
    private int progressPercentage;
    private String message;
}
