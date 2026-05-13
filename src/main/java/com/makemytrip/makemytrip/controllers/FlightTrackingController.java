package com.makemytrip.makemytrip.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.makemytrip.makemytrip.services.FlightTrackingService;

@RestController
@RequestMapping("/api/tracking")
@CrossOrigin(origins = "*") // Allows your frontend to connect
public class FlightTrackingController {

    @Autowired
    private FlightTrackingService trackingService;

    private List<SseEmitter> emitters = new ArrayList<>();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // UPDATED CORRECT VERSION
    public SseEmitter subscribe(String flightId) {
        // You can use the flightId here later if you want to track 
        // specific flights, but for now, just adding the parameter 
        // will fix the compilation error.
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        this.emitters.add(emitter);
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        return emitter;
    }
}
