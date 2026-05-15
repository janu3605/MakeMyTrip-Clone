package com.makemytrip.makemytrip.services;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.makemytrip.makemytrip.models.Flight;
import com.makemytrip.makemytrip.models.Hotel;
import com.makemytrip.makemytrip.models.PriceHistory;
import com.makemytrip.makemytrip.models.SeasonalRule;
import com.makemytrip.makemytrip.repositories.FlightRepository;
import com.makemytrip.makemytrip.repositories.HotelRepository;
import com.makemytrip.makemytrip.repositories.PriceHistoryRepository;
import com.makemytrip.makemytrip.repositories.SeasonalRuleRepository;

@Service
public class DynamicPricingService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private SeasonalRuleRepository seasonalRuleRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    // In-memory cache of base prices so we never compound
    private final Map<String, Double> basePriceCache = new ConcurrentHashMap<>();

    // Assumed initial capacity for demand calculation
    private static final int DEFAULT_INITIAL_SEATS = 180;
    private static final int DEFAULT_INITIAL_ROOMS = 50;

    /**
     * Main pricing loop — runs every 60 seconds.
     * Recalculates dynamic prices for all flights and hotels.
     */
    @Scheduled(fixedRate = 60000)
    public void recalculatePrices() {
        System.out.println("[PricingEngine] Tick: Recalculating dynamic prices...");

        List<SeasonalRule> activeRules = seasonalRuleRepository.findByActiveTrue();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // --- FLIGHTS ---
        List<Flight> flights = flightRepository.findAll();
        for (Flight flight : flights) {
            try {
                double base = getOrSetBasePrice("FLIGHT_" + flight.getId(), flight.getPrice(), flight.getBasePrice());

                // 1. Demand multiplier (based on seat scarcity)
                double demandMultiplier = calculateDemandMultiplier(
                        flight.getAvailableSeats(), DEFAULT_INITIAL_SEATS);

                // 2. Seasonal multiplier
                double seasonalMultiplier = calculateSeasonalMultiplier(activeRules, today);

                // 3. Time-to-departure multiplier (flights only)
                double timeFactor = calculateTimeToDepMultiplier(flight.getDepartureTime(), now);

                double combinedMultiplier = demandMultiplier * seasonalMultiplier * timeFactor;
                double newPrice = Math.round(base * combinedMultiplier * 100.0) / 100.0;

                // Build reason string
                String reason = buildReason(demandMultiplier, seasonalMultiplier, timeFactor);

                // Only update if price actually changed
                if (Math.abs(newPrice - flight.getPrice()) > 0.01) {
                    PriceHistory history = new PriceHistory(
                            flight.getId(), "FLIGHT", flight.getPrice(), newPrice,
                            combinedMultiplier, reason);
                    priceHistoryRepository.save(history);

                    flight.setPrice(newPrice);
                    if (flight.getBasePrice() == 0) {
                        flight.setBasePrice(base);
                    }
                    flightRepository.save(flight);
                    System.out.println("[PricingEngine] Flight " + flight.getId()
                            + ": ₹" + base + " → ₹" + newPrice + " (" + reason + ")");
                }
            } catch (Exception e) {
                System.out.println("[PricingEngine] Error pricing flight " + flight.getId() + ": " + e.getMessage());
            }
        }

        // --- HOTELS ---
        List<Hotel> hotels = hotelRepository.findAll();
        for (Hotel hotel : hotels) {
            try {
                double base = getOrSetBasePrice("HOTEL_" + hotel.getId(),
                        hotel.getPricePerNight(), hotel.getBasePricePerNight());

                // 1. Demand multiplier (based on room scarcity)
                double demandMultiplier = calculateDemandMultiplier(
                        hotel.getAvailableRooms(), DEFAULT_INITIAL_ROOMS);

                // 2. Seasonal multiplier
                double seasonalMultiplier = calculateSeasonalMultiplier(activeRules, today);

                // Hotels don't have time-to-departure, so factor = 1.0
                double combinedMultiplier = demandMultiplier * seasonalMultiplier;
                double newPrice = Math.round(base * combinedMultiplier * 100.0) / 100.0;

                String reason = buildReason(demandMultiplier, seasonalMultiplier, 1.0);

                if (Math.abs(newPrice - hotel.getPricePerNight()) > 0.01) {
                    PriceHistory history = new PriceHistory(
                            hotel.getId(), "HOTEL", hotel.getPricePerNight(), newPrice,
                            combinedMultiplier, reason);
                    priceHistoryRepository.save(history);

                    hotel.setPricePerNight(newPrice);
                    if (hotel.getBasePricePerNight() == 0) {
                        hotel.setBasePricePerNight(base);
                    }
                    hotelRepository.save(hotel);
                    System.out.println("[PricingEngine] Hotel " + hotel.getId()
                            + ": ₹" + base + " → ₹" + newPrice + " (" + reason + ")");
                }
            } catch (Exception e) {
                System.out.println("[PricingEngine] Error pricing hotel " + hotel.getId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Gets the base price from cache, or initializes it.
     * This prevents price compounding across ticks.
     */
    private double getOrSetBasePrice(String key, double currentPrice, double storedBasePrice) {
        return basePriceCache.computeIfAbsent(key, k -> {
            if (storedBasePrice > 0) {
                return storedBasePrice;
            }
            return currentPrice;
        });
    }

    /**
     * Demand multiplier based on availability percentage.
     * <= 10% available → 1.30x
     * <= 20% available → 1.15x
     * <= 40% available → 1.05x
     * Otherwise → 1.0x
     */
    private double calculateDemandMultiplier(int available, int initialCapacity) {
        if (initialCapacity <= 0) return 1.0;
        double ratio = (double) available / initialCapacity;

        if (ratio <= 0.10) return 1.30;
        if (ratio <= 0.20) return 1.15;
        if (ratio <= 0.40) return 1.05;
        return 1.0;
    }

    /**
     * Checks all active seasonal rules. Uses the highest multiplier if
     * multiple rules overlap on today's date.
     */
    private double calculateSeasonalMultiplier(List<SeasonalRule> rules, LocalDate today) {
        double maxMultiplier = 1.0;
        for (SeasonalRule rule : rules) {
            try {
                LocalDate start = LocalDate.parse(rule.getStartDate());
                LocalDate end = LocalDate.parse(rule.getEndDate());
                if (!today.isBefore(start) && !today.isAfter(end)) {
                    maxMultiplier = Math.max(maxMultiplier, rule.getMultiplier());
                }
            } catch (Exception e) {
                // Skip malformed rules
            }
        }
        return maxMultiplier;
    }

    /**
     * Time-to-departure multiplier for flights.
     * < 1 day → 1.25x
     * < 3 days → 1.10x
     * Otherwise → 1.0x
     */
    private double calculateTimeToDepMultiplier(String departureTimeStr, LocalDateTime now) {
        if (departureTimeStr == null || departureTimeStr.trim().isEmpty()) return 1.0;

        try {
            LocalDateTime departure;
            departureTimeStr = departureTimeStr.trim();

            if (departureTimeStr.contains("T")) {
                departure = LocalDateTime.parse(departureTimeStr);
            } else if (departureTimeStr.contains("-") && departureTimeStr.contains(":")) {
                departure = LocalDateTime.parse(departureTimeStr.replace(" ", "T"));
            } else if (departureTimeStr.contains("-")) {
                departure = LocalDate.parse(departureTimeStr).atStartOfDay();
            } else if (departureTimeStr.contains(":")) {
                String[] parts = departureTimeStr.split(":");
                int h = Integer.parseInt(parts[0].trim());
                int m = Integer.parseInt(parts[1].trim());
                departure = now.withHour(h).withMinute(m).withSecond(0);
            } else {
                return 1.0;
            }

            long hoursUntilDeparture = Duration.between(now, departure).toHours();

            if (hoursUntilDeparture <= 0) return 1.0; // Already departed
            if (hoursUntilDeparture < 24) return 1.25;
            if (hoursUntilDeparture < 72) return 1.10;

        } catch (Exception e) {
            // Parsing failed, no surcharge
        }
        return 1.0;
    }

    /**
     * Builds a human-readable reason string from the multiplier values.
     */
    private String buildReason(double demand, double seasonal, double time) {
        StringBuilder sb = new StringBuilder();
        if (demand > 1.0) sb.append("DEMAND_SURGE ");
        if (seasonal > 1.0) sb.append("SEASONAL ");
        if (time > 1.0) sb.append("LAST_MINUTE ");
        if (sb.length() == 0) sb.append("BASE_PRICE");
        return sb.toString().trim();
    }
}
