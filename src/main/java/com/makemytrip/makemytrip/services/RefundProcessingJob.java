package com.makemytrip.makemytrip.services;

import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.models.Users.Booking;
import com.makemytrip.makemytrip.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RefundProcessingJob {

    @Autowired
    private UserRepository userRepository;

    @Scheduled(fixedRate = 120000) // Runs every 2 minutes
    public void processRefunds() {
        List<Users> users = userRepository.findAll();
        boolean anyUpdated = false;

        for (Users user : users) {
            boolean userUpdated = false;
            for (Booking booking : user.getBookings()) {
                if ("CANCELLED".equals(booking.getStatus())) {
                    if ("PENDING".equals(booking.getRefundStatus())) {
                        booking.setRefundStatus("PROCESSED");
                        userUpdated = true;
                    } else if ("PROCESSED".equals(booking.getRefundStatus())) {
                        booking.setRefundStatus("COMPLETED");
                        user.setMockBalance(user.getMockBalance() + booking.getRefundAmount());
                        userUpdated = true;
                    }
                }
            }
            if (userUpdated) {
                userRepository.save(user);
                anyUpdated = true;
            }
        }

        if (anyUpdated) {
            System.out.println("RefundProcessingJob: Updated refund statuses.");
        }
    }
}
