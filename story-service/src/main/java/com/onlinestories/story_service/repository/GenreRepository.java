package com.onlinestories.story_service.repository;

import com.onlinestories.story_service.entity.Genre;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends MongoRepository<Genre, String> {
    Genre findByName(String name);
}
