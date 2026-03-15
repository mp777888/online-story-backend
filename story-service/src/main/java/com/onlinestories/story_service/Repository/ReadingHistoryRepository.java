package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends MongoRepository<ReadingHistory, String> {
    Optional<ReadingHistory> findByUserIdAndStoryId(String userId, String storyId);
    Page<ReadingHistory> findByUserId(String userId, Pageable pageable);
}
