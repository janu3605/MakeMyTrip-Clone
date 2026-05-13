package com.makemytrip.makemytrip.controllers;

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
@CrossOrigin(origins = "*")
public class FlightTrackingController {

    @Autowired
    private FlightTrackingService trackingService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return trackingService.subscribe();
    }
}
