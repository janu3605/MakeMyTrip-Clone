package com.makemytrip.makemytrip.repositories;

import com.makemytrip.makemytrip.models.SeasonalRule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SeasonalRuleRepository extends MongoRepository<SeasonalRule, String> {
    List<SeasonalRule> findByActiveTrue();
}
