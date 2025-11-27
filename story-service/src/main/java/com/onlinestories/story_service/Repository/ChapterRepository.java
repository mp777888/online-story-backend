package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.Chapter;
import com.onlinestories.story_service.Entity.Story;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends MongoRepository<Chapter, String> {
}
