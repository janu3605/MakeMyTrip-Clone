package com.makemytrip.makemytrip.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.makemytrip.makemytrip.models.Recommendation;

public interface RecommendationRepository extends MongoRepository<Recommendation, String> {

    List<Recommendation> findByUserId(String userId);

    List<Recommendation> findByUserIdAndEntityType(String userId, String entityType);

    List<Recommendation> findByUserIdAndFeedback(String userId, String feedback);

    List<Recommendation> findByUserIdAndEntityId(String userId, String entityId);

    void deleteByUserId(String userId);
}
