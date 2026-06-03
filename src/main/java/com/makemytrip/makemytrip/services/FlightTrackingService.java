package com.makemytrip.makemytrip.services;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
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
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        return emitter;
    }

    private LocalDateTime parseSafeDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.now().plusDays(1);
        }
        try {
            dateStr = dateStr.trim();
            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr);
            }
            if (dateStr.contains("-") && !dateStr.contains(":")) {
                return LocalDate.parse(dateStr).atStartOfDay();
            }
            if (dateStr.contains("-") && dateStr.contains(":")) {
                return LocalDateTime.parse(dateStr.replace(" ", "T"));
            }
            if (dateStr.contains(":")) {
                String[] parts = dateStr.split(":");
                int h = Integer.parseInt(parts[0].trim());
                int m = Integer.parseInt(parts[1].trim());
                return LocalDateTime.now().withHour(h).withMinute(m).withSecond(0);
            }
        } catch (Exception e) {
        }
        return LocalDateTime.now().plusDays(1); 
    }

    private static final String[] DELAY_REASONS = {
            "Adverse weather conditions at departure airport",
            "Air traffic congestion causing holding pattern",
            "Technical maintenance check required",
            "Late arrival of incoming aircraft",
            "Crew scheduling adjustment",
            "Runway maintenance at destination airport",
            "Security clearance delay",
            "Baggage loading delay due to heavy volume",
            "De-icing procedure required",
            "Airspace restriction imposed by ATC"
    };

    private final java.util.Random random = new java.util.Random();

    @Scheduled(fixedRate = 10000)
    public void tick() {
        if (emitters.isEmpty()) {
            return;
        }

        List<Flight> allFlights = flightRepository.findAll();
        Map<String, FlightStatusUpdate> updates = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (Flight f : allFlights) {
            try {
                LocalDateTime dep = parseSafeDate(f.getDepartureTime());
                LocalDateTime arr = parseSafeDate(f.getArrivalTime());

                String status;
                int progress = 0;
                String message = "On Time";
                String reason = customReasons.getOrDefault(f.getId(), "N/A");

                if (now.isBefore(dep.minusMinutes(10))) {
                    status = "SCHEDULED";
                    message = "Check-in Open";
                } else if (now.isBefore(dep)) {
                    status = "BOARDING";
                    message = "Boarding at Gate " + (random.nextInt(20) + 1);
                } else if (now.isAfter(arr)) {
                    status = "LANDED";
                    progress = 100;
                    message = "Arrived Safely";

                    customReasons.remove(f.getId());
                } else {

                    long total = Duration.between(dep, arr).toSeconds();
                    long elapsed = Duration.between(dep, now).toSeconds();
                    progress = total > 0 ? (int) ((elapsed * 100) / total) : 100;
                    progress = Math.min(Math.max(progress, 0), 100);


                    if (!customReasons.containsKey(f.getId()) && random.nextInt(100) < 15) {
                        String delayReason = DELAY_REASONS[random.nextInt(DELAY_REASONS.length)];
                        customReasons.put(f.getId(), delayReason);
                    }

                    if (customReasons.containsKey(f.getId())) {
                        status = "DELAYED";
                        reason = customReasons.get(f.getId());
                        int delayMinutes = 15 + random.nextInt(60);
                        message = "Delayed by approx. " + delayMinutes + " mins — " + reason;
                    } else {
                        status = "IN_AIR";
                        message = "En Route — cruising at FL" + (300 + random.nextInt(100));
                    }
                }

                updates.put(f.getId(), new FlightStatusUpdate(
                        f.getId(), status, message, reason, progress
                ));
            } catch (Exception e) {
            }
        }

        if (!updates.isEmpty()) {
            broadcast(updates);
        }
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
