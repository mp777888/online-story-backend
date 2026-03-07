package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.Genre;
import com.onlinestories.story_service.Entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends MongoRepository<Story, String> {
    Page<Story> findByGenresGenreId(String genreId, Pageable pageable);
    Page<Story> findByAuthorId(String authorId, Pageable pageable);
    Page<Story> findByAuthorIdAndIsPublishedTrue(String authorId, Pageable pageable);
}
