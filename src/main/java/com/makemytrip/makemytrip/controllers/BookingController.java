package com.makemytrip.makemytrip.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.services.BookingService;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/flight")
    public Users.Booking bookFlight(@RequestParam String userId, @RequestParam String flightId, @RequestParam int seats, @RequestParam double price, @RequestParam(required = false, defaultValue = "") String selectedSeat, @RequestParam(required = false, defaultValue = "0") double seatPremium) {
        return bookingService.bookFlight(userId, flightId, seats, price, selectedSeat, seatPremium);
    }

    @PostMapping("/hotel")
    public Users.Booking bookhotel(@RequestParam String userId, @RequestParam String hotelId, @RequestParam int rooms, @RequestParam double price, @RequestParam(required = false, defaultValue = "") String selectedRoom, @RequestParam(required = false, defaultValue = "0") double roomPremium) {
        return bookingService.bookhotel(userId, hotelId, rooms, price, selectedRoom, roomPremium);
    }
    @PostMapping("/cancel")
    public Users.Booking cancelBooking(@RequestParam String userId, @RequestParam String bookingId, @RequestParam String reason) {
        return bookingService.cancelBooking(userId, bookingId, reason);
    }
}
