package com.makemytrip.makemytrip.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.makemytrip.makemytrip.models.Flight;
import com.makemytrip.makemytrip.models.Hotel;
import com.makemytrip.makemytrip.models.PriceFreeze;
import com.makemytrip.makemytrip.repositories.FlightRepository;
import com.makemytrip.makemytrip.repositories.HotelRepository;
import com.makemytrip.makemytrip.repositories.PriceFreezeRepository;
import com.makemytrip.makemytrip.repositories.UserRepository;
import com.makemytrip.makemytrip.models.Users;

@Service
public class PriceFreezeService {

    @Autowired
    private PriceFreezeRepository priceFreezeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private HotelRepository hotelRepository;

    public double calculateFreezeFee(int quantity) {
        if (quantity <= 1) return 99.0;
        if (quantity <= 3) return 149.0;
        return 299.0;
    }

    public PriceFreeze freezePrice(String userId, String entityId, String entityType, int quantity) {

        Optional<PriceFreeze> existing = priceFreezeRepository
                .findByUserIdAndEntityIdAndActiveTrue(userId, entityId);
        if (existing.isPresent()) {
            throw new RuntimeException("You already have an active price freeze for this item.");
        }


        double currentPrice = getCurrentPrice(entityId, entityType);
        double fee = calculateFreezeFee(quantity);

        Optional<Users> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        Users user = userOpt.get();
        if (user.getMockBalance() < fee) {
            throw new RuntimeException("Insufficient mock balance to freeze price");
        }
        

        user.setMockBalance(user.getMockBalance() - fee);
        userRepository.save(user);

        PriceFreeze freeze = new PriceFreeze(userId, entityId, entityType,
                currentPrice, fee, quantity);
        return priceFreezeRepository.save(freeze);
    }

    public PriceFreeze getActiveFreeze(String userId, String entityId) {
        Optional<PriceFreeze> freeze = priceFreezeRepository
                .findByUserIdAndEntityIdAndActiveTrue(userId, entityId);

        if (freeze.isPresent()) {
            PriceFreeze pf = freeze.get();

            if (pf.getExpiresAt().isBefore(LocalDateTime.now())) {
                pf.setActive(false);
                priceFreezeRepository.save(pf);
                return null;
            }
            return pf;
        }
        return null;
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredFreezes() {
        List<PriceFreeze> expired = priceFreezeRepository
                .findAllByActiveTrueAndExpiresAtBefore(LocalDateTime.now());

        if (!expired.isEmpty()) {
            for (PriceFreeze pf : expired) {
                pf.setActive(false);
            }
            priceFreezeRepository.saveAll(expired);
        }
    }

    private double getCurrentPrice(String entityId, String entityType) {
        if ("FLIGHT".equalsIgnoreCase(entityType)) {
            Optional<Flight> flight = flightRepository.findById(entityId);
            if (flight.isPresent()) {
                return flight.get().getPrice();
            }
        } else if ("HOTEL".equalsIgnoreCase(entityType)) {
            Optional<Hotel> hotel = hotelRepository.findById(entityId);
            if (hotel.isPresent()) {
                return hotel.get().getPricePerNight();
            }
        }
        throw new RuntimeException("Entity not found: " + entityType + "/" + entityId);
    }
}
