package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.StoryDailyView;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryDailyViewRepository extends MongoRepository<StoryDailyView, String> {
}
