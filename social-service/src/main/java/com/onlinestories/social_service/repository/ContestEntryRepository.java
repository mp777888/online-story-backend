package com.onlinestories.social_service.repository;

import com.onlinestories.social_service.model.ContestEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestEntryRepository extends MongoRepository<ContestEntry, String> {
    List<ContestEntry> findByContestId(String contestId);
    boolean existsByContestIdAndUserId(String contestId, String userId);
}

