package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.models.Users.Booking;
import com.makemytrip.makemytrip.models.Flight;
import com.makemytrip.makemytrip.models.Hotel;
import com.makemytrip.makemytrip.repositories.UserRepository;
import com.makemytrip.makemytrip.repositories.FlightRepository;
import com.makemytrip.makemytrip.repositories.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private HotelRepository hotelRepository;

    public Booking bookFlight(String userId, String flightId, int seats, double price, String selectedSeat, double seatPremium) {
        Optional<Users> usersOptional = userRepository.findById(userId);
        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (usersOptional.isPresent() && flightOptional.isPresent()) {
            Users user = usersOptional.get();
            Flight flight = flightOptional.get();

            if (user.getMockBalance() < price) {
                throw new RuntimeException("Insufficient mock balance");
            }

            if (flight.getAvailableSeats() >= seats) {
                flight.setAvailableSeats(flight.getAvailableSeats() - seats);
                flightRepository.save(flight);

                Booking booking = new Booking();
                booking.setType("Flight");
                booking.setBookingId(flightId);
                booking.setDate(LocalDate.now().toString());
                booking.setQuantity(seats);
                booking.setTotalPrice(price);
                booking.setSelectedSeat(selectedSeat);
                booking.setSeatRoomPremium(seatPremium);
                user.getBookings().add(booking);
                user.setMockBalance(user.getMockBalance() - price);
                userRepository.save(user);
                return booking;
            } else {
                throw new RuntimeException("Not enough seats available");
            }
        }
        throw new RuntimeException("User or flight not found");
    }

    public Booking bookhotel(String userId, String hotelId, int rooms, double price, String selectedRoom, double roomPremium) {
        Optional<Users> usersOptional = userRepository.findById(userId);
        Optional<Hotel> hotelOptional = hotelRepository.findById(hotelId);
        if (usersOptional.isPresent() && hotelOptional.isPresent()) {
            Users user = usersOptional.get();
            Hotel hotel = hotelOptional.get();

            if (user.getMockBalance() < price) {
                throw new RuntimeException("Insufficient mock balance");
            }

            if (hotel.getAvailableRooms() >= rooms) {
                hotel.setAvailableRooms(hotel.getAvailableRooms() - rooms);
                hotelRepository.save(hotel);

                Booking booking = new Booking();
                booking.setType("Hotel");
                booking.setBookingId(hotelId);
                booking.setDate(LocalDate.now().toString());
                booking.setQuantity(rooms);
                booking.setTotalPrice(price);
                booking.setSelectedRoom(selectedRoom);
                booking.setSeatRoomPremium(roomPremium);
                user.getBookings().add(booking);
                user.setMockBalance(user.getMockBalance() - price);
                userRepository.save(user);
                return booking;
            } else {
                throw new RuntimeException("Not enough rooms available");
            }
        }
        throw new RuntimeException("User or hotel not found");
    }

    public Booking cancelBooking(String userId, String uniqueBookingId, String reason) {
        Optional<Users> usersOptional = userRepository.findById(userId);
        if (usersOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        Users user = usersOptional.get();

        Booking targetBooking = null;
        for (Booking b : user.getBookings()) {
            if (b.getId().equals(uniqueBookingId)) {
                targetBooking = b;
                break;
            }
        }

        if (targetBooking == null) {
            throw new RuntimeException("Booking not found");
        }

        if ("CANCELLED".equals(targetBooking.getStatus())) {
            throw new RuntimeException("Booking is already cancelled");
        }


        LocalDate bookingDate = LocalDate.parse(targetBooking.getDate());
        long daysBetween = ChronoUnit.DAYS.between(bookingDate, LocalDate.now());
        
        double refundPercent = 0.0;
        if (daysBetween <= 1) {
            refundPercent = 0.50;
        } else {
            refundPercent = 0.20;
        }

        targetBooking.setStatus("CANCELLED");
        targetBooking.setCancellationReason(reason);
        targetBooking.setRefundAmount(targetBooking.getTotalPrice() * refundPercent);
        targetBooking.setRefundStatus("PENDING");
        targetBooking.setCancellationDate(LocalDateTime.now().toString());


        if ("Flight".equalsIgnoreCase(targetBooking.getType())) {
            Optional<Flight> flightOpt = flightRepository.findById(targetBooking.getBookingId());
            if (flightOpt.isPresent()) {
                Flight f = flightOpt.get();
                f.setAvailableSeats(f.getAvailableSeats() + targetBooking.getQuantity());
                flightRepository.save(f);
            }
        } else if ("Hotel".equalsIgnoreCase(targetBooking.getType())) {
            Optional<Hotel> hotelOpt = hotelRepository.findById(targetBooking.getBookingId());
            if (hotelOpt.isPresent()) {
                Hotel h = hotelOpt.get();
                h.setAvailableRooms(h.getAvailableRooms() + targetBooking.getQuantity());
                hotelRepository.save(h);
            }
        }

        userRepository.save(user);
        return targetBooking;
    }

}
