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
            System.out.println("Could not parse date: " + dateStr);
        }
        return LocalDateTime.now().plusDays(1); 
    }

    @Scheduled(fixedRate = 10000)
    public void tick() {
        if (emitters.isEmpty()) {
            return;
        }
        System.out.println("Radar Tick: Pushing to " + emitters.size() + " clients.");

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
                    long total = Duration.between(dep, arr).toSeconds();
                    long elapsed = Duration.between(dep, now).toSeconds();
                    progress = total > 0 ? (int) ((elapsed * 100) / total) : 100;
                    progress = Math.min(Math.max(progress, 0), 100);
                    message = "En Route";
                }

                updates.put(f.getId(), new FlightStatusUpdate(
                        f.getId(), status, message, customReasons.getOrDefault(f.getId(), "N/A"), progress
                ));
            } catch (Exception e) {
                System.out.println("Format error on Flight " + f.getId());
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
