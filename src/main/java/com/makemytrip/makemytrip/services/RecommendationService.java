package com.makemytrip.makemytrip.services;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.makemytrip.makemytrip.dto.RecommendationResponse;
import com.makemytrip.makemytrip.models.Flight;
import com.makemytrip.makemytrip.models.Hotel;
import com.makemytrip.makemytrip.models.Recommendation;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.models.Users.Booking;
import com.makemytrip.makemytrip.repositories.FlightRepository;
import com.makemytrip.makemytrip.repositories.HotelRepository;
import com.makemytrip.makemytrip.repositories.RecommendationRepository;
import com.makemytrip.makemytrip.repositories.UserRepository;

@Service
public class RecommendationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    // ====== Destination → Category mapping ======
    private static final Map<String, String> DESTINATION_CATEGORIES = new HashMap<>();
    private static final Map<String, String> DESTINATION_IMAGES = new HashMap<>();

    static {
        // Beach destinations
        DESTINATION_CATEGORIES.put("goa", "beach");
        DESTINATION_CATEGORIES.put("bali", "beach");
        DESTINATION_CATEGORIES.put("maldives", "beach");
        DESTINATION_CATEGORIES.put("phuket", "beach");
        DESTINATION_CATEGORIES.put("kochi", "beach");
        DESTINATION_CATEGORIES.put("pondicherry", "beach");
        DESTINATION_CATEGORIES.put("andaman", "beach");
        DESTINATION_CATEGORIES.put("vizag", "beach");
        DESTINATION_CATEGORIES.put("kovalam", "beach");
        DESTINATION_CATEGORIES.put("mumbai", "beach");

        // Mountain destinations
        DESTINATION_CATEGORIES.put("shimla", "mountain");
        DESTINATION_CATEGORIES.put("manali", "mountain");
        DESTINATION_CATEGORIES.put("darjeeling", "mountain");
        DESTINATION_CATEGORIES.put("mussoorie", "mountain");
        DESTINATION_CATEGORIES.put("nainital", "mountain");
        DESTINATION_CATEGORIES.put("ooty", "mountain");
        DESTINATION_CATEGORIES.put("coorg", "mountain");
        DESTINATION_CATEGORIES.put("leh", "mountain");
        DESTINATION_CATEGORIES.put("ladakh", "mountain");
        DESTINATION_CATEGORIES.put("srinagar", "mountain");
        DESTINATION_CATEGORIES.put("munnar", "mountain");

        // City destinations
        DESTINATION_CATEGORIES.put("delhi", "city");
        DESTINATION_CATEGORIES.put("bangalore", "city");
        DESTINATION_CATEGORIES.put("bengaluru", "city");
        DESTINATION_CATEGORIES.put("hyderabad", "city");
        DESTINATION_CATEGORIES.put("chennai", "city");
        DESTINATION_CATEGORIES.put("kolkata", "city");
        DESTINATION_CATEGORIES.put("pune", "city");
        DESTINATION_CATEGORIES.put("ahmedabad", "city");
        DESTINATION_CATEGORIES.put("singapore", "city");
        DESTINATION_CATEGORIES.put("dubai", "city");
        DESTINATION_CATEGORIES.put("bangkok", "city");

        // Heritage destinations
        DESTINATION_CATEGORIES.put("jaipur", "heritage");
        DESTINATION_CATEGORIES.put("udaipur", "heritage");
        DESTINATION_CATEGORIES.put("agra", "heritage");
        DESTINATION_CATEGORIES.put("varanasi", "heritage");
        DESTINATION_CATEGORIES.put("jodhpur", "heritage");
        DESTINATION_CATEGORIES.put("mysore", "heritage");
        DESTINATION_CATEGORIES.put("hampi", "heritage");
        DESTINATION_CATEGORIES.put("khajuraho", "heritage");

        // Category images for fallback
        DESTINATION_IMAGES.put("beach", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800");
        DESTINATION_IMAGES.put("mountain", "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=800");
        DESTINATION_IMAGES.put("city", "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=800");
        DESTINATION_IMAGES.put("heritage", "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?auto=format&fit=crop&w=800");
    }

    /**
     * Get personalized recommendations for a user.
     */
    public List<RecommendationResponse> getRecommendations(String userId, String type) {
        Optional<Users> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return getTrendingRecommendations();
        }

        Users user = userOpt.get();
        List<Booking> bookings = user.getBookings();

        if (bookings == null || bookings.isEmpty()) {
            return getTrendingRecommendations();
        }

        // Collect previously irrelevant entity IDs to skip
        List<Recommendation> irrelevant = recommendationRepository.findByUserIdAndFeedback(userId, "IRRELEVANT");
        Set<String> skipEntityIds = irrelevant.stream()
                .map(Recommendation::getEntityId)
                .collect(Collectors.toSet());

        // Collect already-booked entity IDs
        Set<String> bookedIds = bookings.stream()
                .map(Booking::getBookingId)
                .collect(Collectors.toSet());

        List<Recommendation> candidates = new ArrayList<>();

        // 1. Repeat Destination Affinity
        candidates.addAll(generateRepeatDestinationRecs(user, bookings, skipEntityIds, bookedIds));

        // 2. Category-Based
        candidates.addAll(generateCategoryRecs(user, bookings, skipEntityIds, bookedIds));

        // 3. Price-Range Matching
        candidates.addAll(generatePriceRangeRecs(user, bookings, skipEntityIds, bookedIds));

        // 4. Collaborative Filtering
        candidates.addAll(generateCollaborativeRecs(user, bookings, skipEntityIds, bookedIds));

        // 5. Fill with trending if not enough
        if (candidates.size() < 6) {
            candidates.addAll(generateTrendingRecs(skipEntityIds, bookedIds));
        }

        // Deduplicate by entityId, keep highest score
        Map<String, Recommendation> deduped = new LinkedHashMap<>();
        for (Recommendation rec : candidates) {
            String key = rec.getEntityId();
            if (!deduped.containsKey(key) || deduped.get(key).getScore() < rec.getScore()) {
                deduped.put(key, rec);
            }
        }

        // Sort by score descending, cap at 12
        List<Recommendation> finalList = deduped.values().stream()
                .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
                .limit(12)
                .collect(Collectors.toList());

        // Filter by type if specified
        if (type != null && !type.isEmpty()) {
            finalList = finalList.stream()
                    .filter(r -> type.equalsIgnoreCase(r.getEntityType()))
                    .collect(Collectors.toList());
        }

        // Persist recommendations
        for (Recommendation rec : finalList) {
            rec.setUserId(userId);
            // Check if we already have this recommendation saved
            List<Recommendation> existing = recommendationRepository.findByUserIdAndEntityId(userId, rec.getEntityId());
            if (existing.isEmpty()) {
                recommendationRepository.save(rec);
            } else {
                // Update score and reason
                Recommendation ex = existing.get(0);
                ex.setScore(rec.getScore());
                ex.setReason(rec.getReason());
                ex.setReasonCode(rec.getReasonCode());
                recommendationRepository.save(ex);
                rec.setId(ex.getId());
                rec.setFeedback(ex.getFeedback());
            }
        }

        return finalList.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Get trending recommendations (for logged-out or new users).
     */
    public List<RecommendationResponse> getTrendingRecommendations() {
        List<Recommendation> trending = generateTrendingRecs(Collections.emptySet(), Collections.emptySet());
        return trending.stream()
                .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
                .limit(12)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Submit feedback on a recommendation.
     */
    public Recommendation submitFeedback(String recommendationId, String userId, String feedback) {
        Optional<Recommendation> recOpt = recommendationRepository.findById(recommendationId);
        if (recOpt.isEmpty()) {
            throw new RuntimeException("Recommendation not found");
        }
        Recommendation rec = recOpt.get();
        rec.setFeedback(feedback);
        rec.setFeedbackDate(Instant.now());
        return recommendationRepository.save(rec);
    }

    // ========== ALGORITHM IMPLEMENTATIONS ==========

    /**
     * Algorithm 1: Repeat Destination Affinity
     * If a user has booked flights/hotels to the same destination multiple times,
     * suggest other options in that same location.
     */
    private List<Recommendation> generateRepeatDestinationRecs(Users user, List<Booking> bookings,
            Set<String> skipEntityIds, Set<String> bookedIds) {
        List<Recommendation> recs = new ArrayList<>();
        List<Flight> allFlights = flightRepository.findAll();
        List<Hotel> allHotels = hotelRepository.findAll();

        // Count destination frequency from flight bookings
        Map<String, Integer> destFrequency = new HashMap<>();
        for (Booking b : bookings) {
            if ("CANCELLED".equals(b.getStatus())) continue;
            if ("Flight".equalsIgnoreCase(b.getType())) {
                Flight flight = allFlights.stream()
                        .filter(f -> f.getId().equals(b.getBookingId()))
                        .findFirst().orElse(null);
                if (flight != null) {
                    destFrequency.merge(flight.getTo().toLowerCase(), 1, Integer::sum);
                }
            } else if ("Hotel".equalsIgnoreCase(b.getType())) {
                Hotel hotel = allHotels.stream()
                        .filter(h -> h.getId().equals(b.getBookingId()))
                        .findFirst().orElse(null);
                if (hotel != null) {
                    destFrequency.merge(hotel.getLocation().toLowerCase(), 1, Integer::sum);
                }
            }
        }

        // For destinations visited 2+ times, suggest other hotels/flights there
        for (Map.Entry<String, Integer> entry : destFrequency.entrySet()) {
            if (entry.getValue() >= 2) {
                String dest = entry.getKey();
                int visits = entry.getValue();

                // Suggest hotels in that location
                for (Hotel h : allHotels) {
                    if (h.getLocation().toLowerCase().contains(dest)
                            && !bookedIds.contains(h.getId())
                            && !skipEntityIds.contains(h.getId())) {
                        Recommendation rec = new Recommendation();
                        rec.setEntityId(h.getId());
                        rec.setEntityType("HOTEL");
                        rec.setScore(85 + Math.min(visits * 3, 15));
                        rec.setReason("You've visited " + capitalize(dest) + " " + visits + " times — here's a top hotel there!");
                        rec.setReasonCode("REPEAT_DESTINATION");
                        rec.setEntityName(h.gethotelName());
                        rec.setEntityLocation(h.getLocation());
                        rec.setEntityPrice(h.getPricePerNight());
                        rec.setEntityImageUrl(getImageForLocation(dest));
                        rec.setEntityCategory(getCategoryForLocation(dest));
                        recs.add(rec);
                    }
                }

                // Suggest flights to that destination
                for (Flight f : allFlights) {
                    if (f.getTo().toLowerCase().contains(dest)
                            && !bookedIds.contains(f.getId())
                            && !skipEntityIds.contains(f.getId())) {
                        Recommendation rec = new Recommendation();
                        rec.setEntityId(f.getId());
                        rec.setEntityType("FLIGHT");
                        rec.setScore(80 + Math.min(visits * 3, 15));
                        rec.setReason("You love " + capitalize(dest) + "! Here's a great flight deal.");
                        rec.setReasonCode("REPEAT_DESTINATION");
                        rec.setEntityName(f.getFlightName());
                        rec.setEntityLocation(f.getFrom() + " → " + f.getTo());
                        rec.setEntityPrice(f.getPrice());
                        rec.setEntityImageUrl(getImageForLocation(dest));
                        rec.setEntityCategory(getCategoryForLocation(dest));
                        recs.add(rec);
                    }
                }
            }
        }

        return recs;
    }

    /**
     * Algorithm 2: Category-Based Recommendations
     * Maps destinations to categories (beach, mountain, etc.) and suggests
     * similar-category destinations the user hasn't visited.
     */
    private List<Recommendation> generateCategoryRecs(Users user, List<Booking> bookings,
            Set<String> skipEntityIds, Set<String> bookedIds) {
        List<Recommendation> recs = new ArrayList<>();
        List<Hotel> allHotels = hotelRepository.findAll();
        List<Flight> allFlights = flightRepository.findAll();

        // Determine user's preferred categories
        Map<String, Integer> categoryFrequency = new HashMap<>();
        Set<String> visitedLocations = new HashSet<>();

        for (Booking b : bookings) {
            if ("CANCELLED".equals(b.getStatus())) continue;
            String location = null;

            if ("Flight".equalsIgnoreCase(b.getType())) {
                Flight flight = allFlights.stream()
                        .filter(f -> f.getId().equals(b.getBookingId()))
                        .findFirst().orElse(null);
                if (flight != null) {
                    location = flight.getTo().toLowerCase();
                }
            } else if ("Hotel".equalsIgnoreCase(b.getType())) {
                Hotel hotel = allHotels.stream()
                        .filter(h -> h.getId().equals(b.getBookingId()))
                        .findFirst().orElse(null);
                if (hotel != null) {
                    location = hotel.getLocation().toLowerCase();
                }
            }

            if (location != null) {
                visitedLocations.add(location);
                String category = getCategoryForLocation(location);
                if (category != null) {
                    categoryFrequency.merge(category, 1, Integer::sum);
                }
            }
        }

        // Find the top category
        String topCategory = categoryFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (topCategory == null) return recs;

        String categoryLabel = getCategoryLabel(topCategory);

        // Suggest hotels in same category but different locations
        for (Hotel h : allHotels) {
            String hLoc = h.getLocation().toLowerCase();
            String hCat = getCategoryForLocation(hLoc);
            if (topCategory.equals(hCat)
                    && !visitedLocations.contains(hLoc)
                    && !bookedIds.contains(h.getId())
                    && !skipEntityIds.contains(h.getId())) {
                Recommendation rec = new Recommendation();
                rec.setEntityId(h.getId());
                rec.setEntityType("HOTEL");
                rec.setScore(75);
                rec.setReason("You liked " + categoryLabel + "! Try " + h.getLocation() + ".");
                rec.setReasonCode("SIMILAR_CATEGORY");
                rec.setEntityName(h.gethotelName());
                rec.setEntityLocation(h.getLocation());
                rec.setEntityPrice(h.getPricePerNight());
                rec.setEntityImageUrl(DESTINATION_IMAGES.getOrDefault(topCategory, getImageForLocation(hLoc)));
                rec.setEntityCategory(topCategory);
                recs.add(rec);
            }
        }

        // Suggest flights to same category destinations
        for (Flight f : allFlights) {
            String fDest = f.getTo().toLowerCase();
            String fCat = getCategoryForLocation(fDest);
            if (topCategory.equals(fCat)
                    && !visitedLocations.contains(fDest)
                    && !bookedIds.contains(f.getId())
                    && !skipEntityIds.contains(f.getId())) {
                Recommendation rec = new Recommendation();
                rec.setEntityId(f.getId());
                rec.setEntityType("FLIGHT");
                rec.setScore(70);
                rec.setReason("You liked " + categoryLabel + "! Explore " + f.getTo() + ".");
                rec.setReasonCode("SIMILAR_CATEGORY");
                rec.setEntityName(f.getFlightName());
                rec.setEntityLocation(f.getFrom() + " → " + f.getTo());
                rec.setEntityPrice(f.getPrice());
                rec.setEntityImageUrl(DESTINATION_IMAGES.getOrDefault(topCategory, getImageForLocation(fDest)));
                rec.setEntityCategory(topCategory);
                recs.add(rec);
            }
        }

        return recs;
    }

    /**
     * Algorithm 3: Price-Range Matching
     * Suggests items within ±30% of the user's average booking price.
     */
    private List<Recommendation> generatePriceRangeRecs(Users user, List<Booking> bookings,
            Set<String> skipEntityIds, Set<String> bookedIds) {
        List<Recommendation> recs = new ArrayList<>();

        double totalPrice = 0;
        int count = 0;
        for (Booking b : bookings) {
            if (!"CANCELLED".equals(b.getStatus())) {
                totalPrice += b.getTotalPrice();
                count++;
            }
        }

        if (count == 0) return recs;
        double avgPrice = totalPrice / count;
        double lowerBound = avgPrice * 0.7;
        double upperBound = avgPrice * 1.3;

        List<Hotel> allHotels = hotelRepository.findAll();
        for (Hotel h : allHotels) {
            if (h.getPricePerNight() >= lowerBound && h.getPricePerNight() <= upperBound
                    && !bookedIds.contains(h.getId())
                    && !skipEntityIds.contains(h.getId())) {
                Recommendation rec = new Recommendation();
                rec.setEntityId(h.getId());
                rec.setEntityType("HOTEL");
                rec.setScore(60);
                rec.setReason("Great value in your preferred price range.");
                rec.setReasonCode("PRICE_RANGE");
                rec.setEntityName(h.gethotelName());
                rec.setEntityLocation(h.getLocation());
                rec.setEntityPrice(h.getPricePerNight());
                rec.setEntityImageUrl(getImageForLocation(h.getLocation().toLowerCase()));
                rec.setEntityCategory(getCategoryForLocation(h.getLocation().toLowerCase()));
                recs.add(rec);
            }
        }

        List<Flight> allFlights = flightRepository.findAll();
        for (Flight f : allFlights) {
            if (f.getPrice() >= lowerBound && f.getPrice() <= upperBound
                    && !bookedIds.contains(f.getId())
                    && !skipEntityIds.contains(f.getId())) {
                Recommendation rec = new Recommendation();
                rec.setEntityId(f.getId());
                rec.setEntityType("FLIGHT");
                rec.setScore(55);
                rec.setReason("Great deal matching your budget preferences.");
                rec.setReasonCode("PRICE_RANGE");
                rec.setEntityName(f.getFlightName());
                rec.setEntityLocation(f.getFrom() + " → " + f.getTo());
                rec.setEntityPrice(f.getPrice());
                rec.setEntityImageUrl(getImageForLocation(f.getTo().toLowerCase()));
                rec.setEntityCategory(getCategoryForLocation(f.getTo().toLowerCase()));
                recs.add(rec);
            }
        }

        return recs;
    }

    /**
     * Algorithm 4: Simplified Collaborative Filtering
     * Finds users with similar booking patterns and recommends items they
     * booked that the current user hasn't.
     */
    private List<Recommendation> generateCollaborativeRecs(Users user, List<Booking> bookings,
            Set<String> skipEntityIds, Set<String> bookedIds) {
        List<Recommendation> recs = new ArrayList<>();
        List<Users> allUsers = userRepository.findAll();
        List<Hotel> allHotels = hotelRepository.findAll();
        List<Flight> allFlights = flightRepository.findAll();

        // Find similar users: those who booked at least 2 of the same entities
        for (Users other : allUsers) {
            if (other.getId().equals(user.getId())) continue;
            if (other.getBookings() == null || other.getBookings().isEmpty()) continue;

            Set<String> otherBookedIds = other.getBookings().stream()
                    .filter(b -> !"CANCELLED".equals(b.getStatus()))
                    .map(Booking::getBookingId)
                    .collect(Collectors.toSet());

            // Count overlap
            long overlap = bookedIds.stream().filter(otherBookedIds::contains).count();

            if (overlap >= 2) {
                // Recommend items the similar user booked that this user hasn't
                for (String entityId : otherBookedIds) {
                    if (!bookedIds.contains(entityId) && !skipEntityIds.contains(entityId)) {
                        // Find the entity details
                        Hotel hotel = allHotels.stream()
                                .filter(h -> h.getId().equals(entityId))
                                .findFirst().orElse(null);
                        if (hotel != null) {
                            Recommendation rec = new Recommendation();
                            rec.setEntityId(hotel.getId());
                            rec.setEntityType("HOTEL");
                            rec.setScore(65 + Math.min(overlap * 5, 20));
                            rec.setReason("Travelers like you also booked this hotel.");
                            rec.setReasonCode("COLLABORATIVE");
                            rec.setEntityName(hotel.gethotelName());
                            rec.setEntityLocation(hotel.getLocation());
                            rec.setEntityPrice(hotel.getPricePerNight());
                            rec.setEntityImageUrl(getImageForLocation(hotel.getLocation().toLowerCase()));
                            rec.setEntityCategory(getCategoryForLocation(hotel.getLocation().toLowerCase()));
                            recs.add(rec);
                            continue;
                        }

                        Flight flight = allFlights.stream()
                                .filter(f -> f.getId().equals(entityId))
                                .findFirst().orElse(null);
                        if (flight != null) {
                            Recommendation rec = new Recommendation();
                            rec.setEntityId(flight.getId());
                            rec.setEntityType("FLIGHT");
                            rec.setScore(60 + Math.min(overlap * 5, 20));
                            rec.setReason("Travelers like you also booked this flight.");
                            rec.setReasonCode("COLLABORATIVE");
                            rec.setEntityName(flight.getFlightName());
                            rec.setEntityLocation(flight.getFrom() + " → " + flight.getTo());
                            rec.setEntityPrice(flight.getPrice());
                            rec.setEntityImageUrl(getImageForLocation(flight.getTo().toLowerCase()));
                            rec.setEntityCategory(getCategoryForLocation(flight.getTo().toLowerCase()));
                            recs.add(rec);
                        }
                    }
                }
            }
        }

        return recs;
    }

    /**
     * Algorithm 5: Trending / Popular
     * Shows globally most-booked items based on all users' booking frequency.
     */
    private List<Recommendation> generateTrendingRecs(Set<String> skipEntityIds, Set<String> bookedIds) {
        List<Recommendation> recs = new ArrayList<>();
        List<Users> allUsers = userRepository.findAll();
        List<Hotel> allHotels = hotelRepository.findAll();
        List<Flight> allFlights = flightRepository.findAll();

        // Count booking frequency for each entity
        Map<String, Integer> entityFrequency = new HashMap<>();
        for (Users u : allUsers) {
            if (u.getBookings() == null) continue;
            for (Booking b : u.getBookings()) {
                if (!"CANCELLED".equals(b.getStatus())) {
                    entityFrequency.merge(b.getBookingId(), 1, Integer::sum);
                }
            }
        }

        // Sort by frequency, pick top items
        List<Map.Entry<String, Integer>> sorted = entityFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toList());

        for (Map.Entry<String, Integer> entry : sorted) {
            String entityId = entry.getKey();
            int bookCount = entry.getValue();

            if (skipEntityIds.contains(entityId) || bookedIds.contains(entityId)) continue;

            Hotel hotel = allHotels.stream()
                    .filter(h -> h.getId().equals(entityId))
                    .findFirst().orElse(null);
            if (hotel != null) {
                Recommendation rec = new Recommendation();
                rec.setEntityId(hotel.getId());
                rec.setEntityType("HOTEL");
                rec.setScore(40 + Math.min(bookCount * 5, 30));
                rec.setReason("Trending this season! Booked by " + bookCount + " travelers.");
                rec.setReasonCode("TRENDING");
                rec.setEntityName(hotel.gethotelName());
                rec.setEntityLocation(hotel.getLocation());
                rec.setEntityPrice(hotel.getPricePerNight());
                rec.setEntityImageUrl(getImageForLocation(hotel.getLocation().toLowerCase()));
                rec.setEntityCategory(getCategoryForLocation(hotel.getLocation().toLowerCase()));
                recs.add(rec);
                continue;
            }

            Flight flight = allFlights.stream()
                    .filter(f -> f.getId().equals(entityId))
                    .findFirst().orElse(null);
            if (flight != null) {
                Recommendation rec = new Recommendation();
                rec.setEntityId(flight.getId());
                rec.setEntityType("FLIGHT");
                rec.setScore(35 + Math.min(bookCount * 5, 30));
                rec.setReason("Trending this season! Booked by " + bookCount + " travelers.");
                rec.setReasonCode("TRENDING");
                rec.setEntityName(flight.getFlightName());
                rec.setEntityLocation(flight.getFrom() + " → " + flight.getTo());
                rec.setEntityPrice(flight.getPrice());
                rec.setEntityImageUrl(getImageForLocation(flight.getTo().toLowerCase()));
                rec.setEntityCategory(getCategoryForLocation(flight.getTo().toLowerCase()));
                recs.add(rec);
            }
        }

        // If we still have too few, fill with random available entities
        if (recs.size() < 6) {
            for (Hotel h : allHotels) {
                if (recs.size() >= 12) break;
                if (!skipEntityIds.contains(h.getId()) && !bookedIds.contains(h.getId())
                        && recs.stream().noneMatch(r -> r.getEntityId().equals(h.getId()))) {
                    Recommendation rec = new Recommendation();
                    rec.setEntityId(h.getId());
                    rec.setEntityType("HOTEL");
                    rec.setScore(30);
                    rec.setReason("Discover something new! This hotel is highly rated.");
                    rec.setReasonCode("TRENDING");
                    rec.setEntityName(h.gethotelName());
                    rec.setEntityLocation(h.getLocation());
                    rec.setEntityPrice(h.getPricePerNight());
                    rec.setEntityImageUrl(getImageForLocation(h.getLocation().toLowerCase()));
                    rec.setEntityCategory(getCategoryForLocation(h.getLocation().toLowerCase()));
                    recs.add(rec);
                }
            }
            for (Flight f : allFlights) {
                if (recs.size() >= 12) break;
                if (!skipEntityIds.contains(f.getId()) && !bookedIds.contains(f.getId())
                        && recs.stream().noneMatch(r -> r.getEntityId().equals(f.getId()))) {
                    Recommendation rec = new Recommendation();
                    rec.setEntityId(f.getId());
                    rec.setEntityType("FLIGHT");
                    rec.setScore(25);
                    rec.setReason("Explore a new destination with this flight!");
                    rec.setReasonCode("TRENDING");
                    rec.setEntityName(f.getFlightName());
                    rec.setEntityLocation(f.getFrom() + " → " + f.getTo());
                    rec.setEntityPrice(f.getPrice());
                    rec.setEntityImageUrl(getImageForLocation(f.getTo().toLowerCase()));
                    rec.setEntityCategory(getCategoryForLocation(f.getTo().toLowerCase()));
                    recs.add(rec);
                }
            }
        }

        return recs;
    }

    // ========== HELPER METHODS ==========

    private RecommendationResponse toResponse(Recommendation rec) {
        RecommendationResponse resp = new RecommendationResponse();
        resp.setId(rec.getId());
        resp.setEntityId(rec.getEntityId());
        resp.setEntityType(rec.getEntityType());
        resp.setScore(rec.getScore());
        resp.setReason(rec.getReason());
        resp.setReasonCode(rec.getReasonCode());
        resp.setFeedback(rec.getFeedback());
        resp.setEntityName(rec.getEntityName());
        resp.setEntityLocation(rec.getEntityLocation());
        resp.setEntityPrice(rec.getEntityPrice());
        resp.setEntityImageUrl(rec.getEntityImageUrl());
        resp.setEntityCategory(rec.getEntityCategory());
        return resp;
    }

    private String getCategoryForLocation(String location) {
        if (location == null) return null;
        String loc = location.toLowerCase().trim();
        for (Map.Entry<String, String> entry : DESTINATION_CATEGORIES.entrySet()) {
            if (loc.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "city"; // default
    }

    private String getCategoryLabel(String category) {
        if (category == null) return "destinations";
        switch (category) {
            case "beach": return "beaches";
            case "mountain": return "mountains";
            case "heritage": return "heritage sites";
            case "city": return "cities";
            default: return "destinations";
        }
    }

    private String getImageForLocation(String location) {
        String cat = getCategoryForLocation(location);
        return DESTINATION_IMAGES.getOrDefault(cat,
                "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800");
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
