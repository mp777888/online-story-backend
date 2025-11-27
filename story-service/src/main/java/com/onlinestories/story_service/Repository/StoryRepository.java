package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.Genre;
import com.onlinestories.story_service.Entity.Story;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryRepository extends MongoRepository<Story, String> {
    List<Story> findByGenresContaining(Genre genre);
}
