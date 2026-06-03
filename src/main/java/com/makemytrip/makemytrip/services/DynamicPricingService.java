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

    private final Map<String, Double> basePriceCache = new ConcurrentHashMap<>();

    private static final int DEFAULT_INITIAL_SEATS = 180;
    private static final int DEFAULT_INITIAL_ROOMS = 50;

    @Scheduled(fixedRate = 60000)
    public void recalculatePrices() {


        List<SeasonalRule> activeRules = seasonalRuleRepository.findByActiveTrue();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();


        List<Flight> flights = flightRepository.findAll();
        for (Flight flight : flights) {
            try {
                double base = getOrSetBasePrice("FLIGHT_" + flight.getId(), flight.getPrice(), flight.getBasePrice());

                double demandMultiplier = calculateDemandMultiplier(
                        flight.getAvailableSeats(), DEFAULT_INITIAL_SEATS);

                double seasonalMultiplier = calculateSeasonalMultiplier(activeRules, today);

                double timeFactor = calculateTimeToDepMultiplier(flight.getDepartureTime(), now);

                double combinedMultiplier = demandMultiplier * seasonalMultiplier * timeFactor;
                double newPrice = Math.round(base * combinedMultiplier * 100.0) / 100.0;

                String reason = buildReason(demandMultiplier, seasonalMultiplier, timeFactor);

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
                }
            } catch (Exception e) {
            }
        }


        List<Hotel> hotels = hotelRepository.findAll();
        for (Hotel hotel : hotels) {
            try {
                double base = getOrSetBasePrice("HOTEL_" + hotel.getId(),
                        hotel.getPricePerNight(), hotel.getBasePricePerNight());

                double demandMultiplier = calculateDemandMultiplier(
                        hotel.getAvailableRooms(), DEFAULT_INITIAL_ROOMS);

                double seasonalMultiplier = calculateSeasonalMultiplier(activeRules, today);

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
                }
            } catch (Exception e) {
            }
        }
    }

    private double getOrSetBasePrice(String key, double currentPrice, double storedBasePrice) {
        return basePriceCache.computeIfAbsent(key, k -> {
            if (storedBasePrice > 0) {
                return storedBasePrice;
            }
            return currentPrice;
        });
    }

    private double calculateDemandMultiplier(int available, int initialCapacity) {
        if (initialCapacity <= 0) return 1.0;
        double ratio = (double) available / initialCapacity;

        if (ratio <= 0.10) return 1.30;
        if (ratio <= 0.20) return 1.15;
        if (ratio <= 0.40) return 1.05;
        return 1.0;
    }

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
            }
        }
        return maxMultiplier;
    }

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
        }
        return 1.0;
    }

    private String buildReason(double demand, double seasonal, double time) {
        StringBuilder sb = new StringBuilder();
        if (demand > 1.0) sb.append("DEMAND_SURGE ");
        if (seasonal > 1.0) sb.append("SEASONAL ");
        if (time > 1.0) sb.append("LAST_MINUTE ");
        if (sb.length() == 0) sb.append("BASE_PRICE");
        return sb.toString().trim();
    }
}
