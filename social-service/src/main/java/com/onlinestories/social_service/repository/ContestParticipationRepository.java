package com.onlinestories.social_service.repository;

import com.onlinestories.social_service.model.ContestParticipation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestParticipationRepository extends MongoRepository<ContestParticipation, String> {
    boolean existsByContestIdAndUserId(String contestId, String userId);
}

