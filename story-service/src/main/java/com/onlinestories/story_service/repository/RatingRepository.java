package com.onlinestories.story_service.repository;

import com.onlinestories.story_service.entity.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends MongoRepository<Rating, String> {
    boolean existsByUserIdAndStoryId(String userId, String storyId);
    List<Rating> findByStoryId(String storyId);
    Rating findByUserIdAndStoryId(String userId, String storyId);
}
