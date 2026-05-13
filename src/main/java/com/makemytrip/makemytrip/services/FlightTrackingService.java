package com.makemytrip.makemytrip.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.makemytrip.makemytrip.dto.FlightStatusUpdate;

@Service
public class FlightTrackingService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Map<String, FlightStatusUpdate> activeFlights = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private final String[] reasons = {
        "Severe Weather Conditions",
        "Late Arrival of Inbound Aircraft",
        "Technical Maintenance Check",
        "Runway Congestion",
        "Crew Scheduling Conflict"
    };

    public SseEmitter subscribe(String flightId) {
        SseEmitter emitter = new SseEmitter(600000L); // 10 minute timeout
        this.emitters.add(emitter);

        // Initialize mock data if this is the first time the flight is being tracked
        activeFlights.putIfAbsent(flightId, new FlightStatusUpdate(
                flightId, "BOARDING", "Gate 12 is now open", "N/A", 0
        ));

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));

        return emitter;
    }

    @Scheduled(fixedRate = 10000) // Simulation ticks every 10 seconds
    public void simulateFlightProgress() {
        activeFlights.forEach((id, flight) -> {
            // 1. Advance progress if in air
            if (flight.getStatus().equals("IN_AIR") && flight.getProgress() < 100) {
                flight.setProgress(Math.min(100, flight.getProgress() + 5));
                if (flight.getProgress() == 100) {
                    flight.setStatus("LANDED");
                    flight.setMessage("Arrived at destination");
                }
            }

            // 2. Randomly trigger boarding to takeoff
            if (flight.getStatus().equals("BOARDING") && random.nextInt(10) > 7) {
                flight.setStatus("IN_AIR");
                flight.setMessage("Flight is currently cruising");
            }

            // 3. Randomly trigger a delay
            if (random.nextInt(20) == 1 && !flight.getStatus().equals("LANDED")) {
                flight.setStatus("DELAYED");
                flight.setReason(reasons[random.nextInt(reasons.length)]);
                flight.setMessage("Departure time revised");
            }

            flight.setLastUpdated(java.time.LocalTime.now().toString());
        });

        broadcastUpdates();
    }

    private void broadcastUpdates() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("flight-status").data(activeFlights));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
