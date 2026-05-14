package com.onlinestories.story_service.repository;

import com.onlinestories.story_service.entity.StoryDailyView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface StoryDailyViewRepository extends MongoRepository<StoryDailyView, String> {
    Page<StoryDailyView> findByStoryId(String storyId, Pageable pageable);

    Page<StoryDailyView> findByStoryIdAndDateBetween(String storyId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
