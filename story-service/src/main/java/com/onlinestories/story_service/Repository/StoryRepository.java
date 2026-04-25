package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.Story;
import com.onlinestories.story_service.Enum.StoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends MongoRepository<Story, String> {
    Page<Story> findByGenresGenreId(String genreId, Pageable pageable);
    Page<Story> findByAuthorId(String authorId, Pageable pageable);
    Page<Story> findByAuthorIdAndStatusNot(String authorId, StoryStatus status, Pageable pageable);
    Page<Story> findByAuthorIdAndStatus(String authorId, StoryStatus status, Pageable pageable);
    Page<Story> findByStatusNot(String status, Pageable pageable);
    Page<Story> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Story> findByStatusNotAndAverageRatingScoreGreaterThan(
            StoryStatus status,
            double minAverageRatingScore,
            Pageable pageable
    );
    Page<Story> findByStatusNotAndNumberOfViewsGreaterThan(
            StoryStatus status,
            int minNumberOfViews,
            Pageable pageable
    );
}
