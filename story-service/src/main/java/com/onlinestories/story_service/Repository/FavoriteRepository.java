package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends MongoRepository<Favorite, String> {
    boolean existsByUserIdAndStoryId(String userId, String storyId);
    Page<Favorite> findByUserId(String userId, Pageable pageable);

    void deleteByUserIdAndStoryId(String userId, String storyId);
}
