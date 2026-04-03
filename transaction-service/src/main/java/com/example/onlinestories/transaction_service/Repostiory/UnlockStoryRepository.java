package com.example.onlinestories.transaction_service.Repostiory;

import com.example.onlinestories.transaction_service.Entity.UnlockStory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnlockStoryRepository extends MongoRepository<UnlockStory, String> {
    UnlockStory findByUserIdAndStoryId(String userId, String storyId);
}
