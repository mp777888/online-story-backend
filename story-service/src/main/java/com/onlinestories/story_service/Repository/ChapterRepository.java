package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.Chapter;
import com.onlinestories.story_service.Enum.ChapterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChapterRepository extends MongoRepository<Chapter, String> {
    List<Chapter> findByStoryId(String storyId);
    Page<Chapter> findByStoryId(String storyId, Pageable pageable);
    Page<Chapter> findByStoryIdAndStatus(String storyId, ChapterStatus status, Pageable pageable);
    boolean existsByChapterId(String chapterId);

    List<Chapter> findByStatusAndPublishedAtLessThanEqual(ChapterStatus chapterStatus, LocalDateTime now);
}
