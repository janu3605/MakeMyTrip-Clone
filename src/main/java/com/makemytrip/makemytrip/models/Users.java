package com.makemytrip.makemytrip.models;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class Users {

    @Id
    private String _id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private String phoneNumber;
    private double mockBalance = 50000.0;
    private List<Booking> bookings = new ArrayList<>();




    public String getFirstName() {
        return firstName;
    }

    public String getId() {
        return _id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public double getMockBalance() {
        return mockBalance;
    }

    public void setMockBalance(double mockBalance) {
        this.mockBalance = mockBalance;
    }

    public static class Booking {

        private String id = java.util.UUID.randomUUID().toString();
        private String type;
        private String bookingId;
        private String date;
        private int quantity;
        private double totalPrice;
        private String status = "CONFIRMED"; // CONFIRMED or CANCELLED
        private String cancellationReason;
        private double refundAmount;
        private String refundStatus; // PENDING, PROCESSED, COMPLETED
        private String cancellationDate;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCancellationReason() {
            return cancellationReason;
        }

        public void setCancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
        }

        public double getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(double refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getRefundStatus() {
            return refundStatus;
        }

        public void setRefundStatus(String refundStatus) {
            this.refundStatus = refundStatus;
        }

        public String getCancellationDate() {
            return cancellationDate;
        }

        public void setCancellationDate(String cancellationDate) {
            this.cancellationDate = cancellationDate;
        }


        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getBookingId() {
            return bookingId;
        }

        public void setBookingId(String bookingId) {
            this.bookingId = bookingId;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
        }

        private String selectedSeat;   // e.g. "5A", null for hotels
        private String selectedRoom;   // e.g. "Floor 3 - Suite 2", null for flights
        private double seatRoomPremium; // extra cost from seat/room tier upgrade

        public String getSelectedSeat() {
            return selectedSeat;
        }

        public void setSelectedSeat(String selectedSeat) {
            this.selectedSeat = selectedSeat;
        }

        public String getSelectedRoom() {
            return selectedRoom;
        }

        public void setSelectedRoom(String selectedRoom) {
            this.selectedRoom = selectedRoom;
        }

        public double getSeatRoomPremium() {
            return seatRoomPremium;
        }

        public void setSeatRoomPremium(double seatRoomPremium) {
            this.seatRoomPremium = seatRoomPremium;
        }
    }
}
