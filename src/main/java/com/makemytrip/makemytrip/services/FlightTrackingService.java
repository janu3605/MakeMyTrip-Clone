package com.makemytrip.makemytrip.services;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.makemytrip.makemytrip.dto.FlightStatusUpdate;
import com.makemytrip.makemytrip.models.Flight;
import com.makemytrip.makemytrip.repositories.FlightRepository;

@Service
public class FlightTrackingService {

    @Autowired
    private FlightRepository flightRepository;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Map<String, String> customReasons = new ConcurrentHashMap<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        this.emitters.add(emitter);
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        return emitter;
    }

    @Scheduled(fixedRate = 10000) // Ticks every 10 seconds
    public void tick() {
        List<Flight> allFlights = flightRepository.findAll();
        Map<String, FlightStatusUpdate> updates = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (Flight f : allFlights) {
            LocalDateTime dep = LocalDateTime.parse(f.getDepartureTime());
            LocalDateTime arr = LocalDateTime.parse(f.getArrivalTime());

            String status;
            int progress = 0;
            String message = "On Time";

            // 1. Calculate Status based on System Clock
            if (now.isBefore(dep.minusMinutes(10))) {
                status = "SCHEDULED";
                message = "Check-in Open";
            } else if (now.isBefore(dep)) {
                status = "BOARDING";
                message = "Boarding at Gate 4";
            } else if (now.isAfter(arr)) {
                status = "LANDED";
                progress = 100;
                message = "Arrived Safely";
            } else {
                status = "IN_AIR";
                // 2. Real-Time Progress Calculation
                long total = Duration.between(dep, arr).toSeconds();
                long elapsed = Duration.between(dep, now).toSeconds();
                progress = (int) ((elapsed * 100) / total);
                message = "En Route";
            }

            updates.put(f.getId(), new FlightStatusUpdate(
                    f.getId(), status, message, customReasons.getOrDefault(f.getId(), "N/A"), progress
            ));
        }
        broadcast(updates);
    }

    private void broadcast(Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("flight-status").data(data));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
