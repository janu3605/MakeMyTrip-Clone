package com.makemytrip.makemytrip.repositories;

import java.util.List;

import com.makemytrip.makemytrip.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByEntityIdAndEntityType(String entityId, String entityType);

    List<Review> findByEntityIdAndEntityTypeAndModerationStatus(String entityId, String entityType, String moderationStatus);

    List<Review> findByUserId(String userId);

    List<Review> findByFlaggedTrue();

    List<Review> findByModerationStatus(String status);

    boolean existsByUserIdAndEntityIdAndEntityType(String userId, String entityId, String entityType);
}
