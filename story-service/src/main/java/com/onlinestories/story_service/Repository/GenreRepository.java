package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.Genre;
import com.onlinestories.story_service.Entity.Story;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends MongoRepository<Genre, String> {
    Genre findByName(String name);
}
