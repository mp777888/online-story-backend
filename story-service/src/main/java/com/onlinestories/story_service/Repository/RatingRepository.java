package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends MongoRepository<Rating, String> {
    boolean existsByUserIdAndStoryId(String userId, String storyId);
    List<Rating> findByStoryId(String storyId);
}
