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

    private static final Map<String, String> DESTINATION_CATEGORIES = new HashMap<>();
    private static final Map<String, String> DESTINATION_IMAGES = new HashMap<>();

    static {
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


        DESTINATION_CATEGORIES.put("jaipur", "heritage");
        DESTINATION_CATEGORIES.put("udaipur", "heritage");
        DESTINATION_CATEGORIES.put("agra", "heritage");
        DESTINATION_CATEGORIES.put("varanasi", "heritage");
        DESTINATION_CATEGORIES.put("jodhpur", "heritage");
        DESTINATION_CATEGORIES.put("mysore", "heritage");
        DESTINATION_CATEGORIES.put("hampi", "heritage");
        DESTINATION_CATEGORIES.put("khajuraho", "heritage");


        DESTINATION_IMAGES.put("beach", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800");
        DESTINATION_IMAGES.put("mountain", "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=800");
        DESTINATION_IMAGES.put("city", "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=800");
        DESTINATION_IMAGES.put("heritage", "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?auto=format&fit=crop&w=800");
    }

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


        List<Recommendation> irrelevant = recommendationRepository.findByUserIdAndFeedback(userId, "IRRELEVANT");
        Set<String> skipEntityIds = irrelevant.stream()
                .map(Recommendation::getEntityId)
                .collect(Collectors.toSet());


        Set<String> bookedIds = bookings.stream()
                .map(Booking::getBookingId)
                .collect(Collectors.toSet());

        List<Recommendation> candidates = new ArrayList<>();


        candidates.addAll(generateRepeatDestinationRecs(user, bookings, skipEntityIds, bookedIds));


        candidates.addAll(generateCategoryRecs(user, bookings, skipEntityIds, bookedIds));


        candidates.addAll(generatePriceRangeRecs(user, bookings, skipEntityIds, bookedIds));


        candidates.addAll(generateCollaborativeRecs(user, bookings, skipEntityIds, bookedIds));


        if (candidates.size() < 6) {
            candidates.addAll(generateTrendingRecs(skipEntityIds, bookedIds));
        }


        Map<String, Recommendation> deduped = new LinkedHashMap<>();
        for (Recommendation rec : candidates) {
            String key = rec.getEntityId();
            if (!deduped.containsKey(key) || deduped.get(key).getScore() < rec.getScore()) {
                deduped.put(key, rec);
            }
        }


        List<Recommendation> finalList = deduped.values().stream()
                .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
                .limit(12)
                .collect(Collectors.toList());


        if (type != null && !type.isEmpty()) {
            finalList = finalList.stream()
                    .filter(r -> type.equalsIgnoreCase(r.getEntityType()))
                    .collect(Collectors.toList());
        }


        for (Recommendation rec : finalList) {
            rec.setUserId(userId);

            List<Recommendation> existing = recommendationRepository.findByUserIdAndEntityId(userId, rec.getEntityId());
            if (existing.isEmpty()) {
                recommendationRepository.save(rec);
            } else {

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

    public List<RecommendationResponse> getTrendingRecommendations() {
        List<Recommendation> trending = generateTrendingRecs(Collections.emptySet(), Collections.emptySet());
        return trending.stream()
                .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
                .limit(12)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

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



    private List<Recommendation> generateRepeatDestinationRecs(Users user, List<Booking> bookings,
            Set<String> skipEntityIds, Set<String> bookedIds) {
        List<Recommendation> recs = new ArrayList<>();
        List<Flight> allFlights = flightRepository.findAll();
        List<Hotel> allHotels = hotelRepository.findAll();


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


        for (Map.Entry<String, Integer> entry : destFrequency.entrySet()) {
            if (entry.getValue() >= 2) {
                String dest = entry.getKey();
                int visits = entry.getValue();


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

    private List<Recommendation> generateCategoryRecs(Users user, List<Booking> bookings,
            Set<String> skipEntityIds, Set<String> bookedIds) {
        List<Recommendation> recs = new ArrayList<>();
        List<Hotel> allHotels = hotelRepository.findAll();
        List<Flight> allFlights = flightRepository.findAll();


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


        String topCategory = categoryFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (topCategory == null) return recs;

        String categoryLabel = getCategoryLabel(topCategory);


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

    private List<Recommendation> generateCollaborativeRecs(Users user, List<Booking> bookings,
            Set<String> skipEntityIds, Set<String> bookedIds) {
        List<Recommendation> recs = new ArrayList<>();
        List<Users> allUsers = userRepository.findAll();
        List<Hotel> allHotels = hotelRepository.findAll();
        List<Flight> allFlights = flightRepository.findAll();


        for (Users other : allUsers) {
            if (other.getId().equals(user.getId())) continue;
            if (other.getBookings() == null || other.getBookings().isEmpty()) continue;

            Set<String> otherBookedIds = other.getBookings().stream()
                    .filter(b -> !"CANCELLED".equals(b.getStatus()))
                    .map(Booking::getBookingId)
                    .collect(Collectors.toSet());


            long overlap = bookedIds.stream().filter(otherBookedIds::contains).count();

            if (overlap >= 2) {

                for (String entityId : otherBookedIds) {
                    if (!bookedIds.contains(entityId) && !skipEntityIds.contains(entityId)) {

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

    private List<Recommendation> generateTrendingRecs(Set<String> skipEntityIds, Set<String> bookedIds) {
        List<Recommendation> recs = new ArrayList<>();
        List<Users> allUsers = userRepository.findAll();
        List<Hotel> allHotels = hotelRepository.findAll();
        List<Flight> allFlights = flightRepository.findAll();


        Map<String, Integer> entityFrequency = new HashMap<>();
        for (Users u : allUsers) {
            if (u.getBookings() == null) continue;
            for (Booking b : u.getBookings()) {
                if (!"CANCELLED".equals(b.getStatus())) {
                    entityFrequency.merge(b.getBookingId(), 1, Integer::sum);
                }
            }
        }


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
