package com.example.onlinestories.transaction_service.repository;

import com.example.onlinestories.transaction_service.entity.UnlockStory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnlockStoryRepository extends MongoRepository<UnlockStory, String> {
    UnlockStory findByUserIdAndStoryId(String userId, String storyId);
}
