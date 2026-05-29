package com.makemytrip.makemytrip.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.makemytrip.makemytrip.models.Flight;
import com.makemytrip.makemytrip.models.Hotel;
import com.makemytrip.makemytrip.models.PriceHistory;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.repositories.FlightRepository;
import com.makemytrip.makemytrip.repositories.HotelRepository;
import com.makemytrip.makemytrip.repositories.PriceHistoryRepository;
import com.makemytrip.makemytrip.repositories.UserRepository;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @GetMapping("/users")
    public ResponseEntity<List<Users>> getallusers() {
        List<Users> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/flight")
    public Flight addflight(@RequestBody Flight flight) {
        // Set the base price before saving
        if (flight.getBasePrice() == 0) {
            flight.setBasePrice(flight.getPrice());
        }
        Flight savedFlight = flightRepository.save(flight);

        // Generate random price history entries (5-10 records over the past 7 days)
        generateRandomPriceHistory(savedFlight.getId(), "FLIGHT", savedFlight.getPrice());

        return savedFlight;
    }

    @PostMapping("/hotel")
    public Hotel addhotel(@RequestBody Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @PutMapping("flight/{id}")
    public ResponseEntity<Flight> editflight(@PathVariable String id, @RequestBody Flight updatedFlight) {
        Optional<Flight> flightOptional = flightRepository.findById(id);
        if (flightOptional.isPresent()) {
            Flight flight = flightOptional.get();
            flight.setFlightName(updatedFlight.getFlightName());
            flight.setFrom(updatedFlight.getFrom());
            flight.setTo(updatedFlight.getTo());
            flight.setDepartureTime(updatedFlight.getDepartureTime());
            flight.setArrivalTime(updatedFlight.getArrivalTime());
            flight.setPrice(updatedFlight.getPrice());
            flight.setAvailableSeats(updatedFlight.getAvailableSeats());
            flightRepository.save(flight);
            return ResponseEntity.ok(flight);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("hotel/{id}")
    public ResponseEntity<Hotel> editHotel(@PathVariable String id, @RequestBody Hotel updatedHotel) {
        Optional<Hotel> hotelOptional = hotelRepository.findById(id);
        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();
            hotel.sethotelName(updatedHotel.gethotelName());
            hotel.setLocation(updatedHotel.getLocation());
            hotel.setAvailableRooms(updatedHotel.getAvailableRooms());
            hotel.setPricePerNight(updatedHotel.getPricePerNight());
            hotel.setamenities((updatedHotel.getamenities()));
            hotelRepository.save(hotel);
            return ResponseEntity.ok(hotel);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Generates 5-10 random price history entries for a newly created entity.
     * Simulates realistic price fluctuations over the past 7 days.
     */
    private void generateRandomPriceHistory(String entityId, String entityType, double basePrice) {
        java.util.Random rand = new java.util.Random();
        int count = 5 + rand.nextInt(6); // 5 to 10 entries
        String[] reasons = {"BASE_PRICE", "DEMAND_SURGE", "SEASONAL", "LAST_MINUTE", "BASE_PRICE", "DEMAND_SURGE"};

        double prevPrice = basePrice;
        for (int i = count; i >= 0; i--) {
            // Spread entries over the past 7 days
            java.time.Instant timestamp = java.time.Instant.now().minus(java.time.Duration.ofHours(i * 24L / count + rand.nextInt(4)));

            // Random variation ±15% from base
            double variation = 0.85 + (rand.nextDouble() * 0.30);
            double newPrice = Math.round(basePrice * variation * 100.0) / 100.0;
            double multiplier = Math.round(variation * 100.0) / 100.0;
            String reason = reasons[rand.nextInt(reasons.length)];

            PriceHistory history = new PriceHistory(entityId, entityType, prevPrice, newPrice, multiplier, reason);
            priceHistoryRepository.save(history);
            prevPrice = newPrice;
        }
    }
}
