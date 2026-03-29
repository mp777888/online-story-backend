package com.onlinestories.story_service.Service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.story_service.DTO.Response.ChapterStatsResponse;
import com.onlinestories.story_service.DTO.Response.StoryDailyViewResponse;
import com.onlinestories.story_service.Entity.Story;
import com.onlinestories.story_service.Entity.StoryDailyView;

import com.onlinestories.story_service.Repository.ChapterRepository;
import com.onlinestories.story_service.Repository.ProgressReadingRepository;
import com.onlinestories.story_service.Repository.StoryDailyViewRepository;
import com.onlinestories.story_service.Repository.StoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AnalyticsService {
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    ProgressReadingRepository progressReadingRepository;
    StoryDailyViewRepository storyDailyViewRepository;

    public Page<StoryDailyViewResponse> getStoryDailyViews(
            String userId, String storyId, int page, int size) {
        log.info("Fetching daily views for story ID: {}, page: {}, size: {}", storyId, page, size);
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

        if (!story.getAuthorId().equals(userId)) {
            log.warn("Unauthorized access attempt by user ID: {} for story ID: {}", userId, storyId);
            throw new AppException(ErrorCode.NOT_AUTHOR_OF_STORY);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<StoryDailyView> storyDailyViews = storyDailyViewRepository.findByStoryId(storyId, pageable);

        return storyDailyViews.map(view -> StoryDailyViewResponse.builder()
                .id(view.getId())
                .storyId(view.getStoryId())
                .date(view.getDate())
                .viewCount(view.getViewCount())
                .build());

    }

    public Page<ChapterStatsResponse> getChapterStats(
           String userId, String storyId, int page, int size) {

        log.info("Fetching chapter stats for story ID: {}, page: {}, size: {}", storyId, page, size);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

        if (!story.getAuthorId().equals(userId)) {
            log.warn("Unauthorized access attempt by user ID: {} for story ID: {}", userId, storyId);
            throw new AppException(ErrorCode.NOT_AUTHOR_OF_STORY);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("chapterId")));

        return chapterRepository.findByStoryId(storyId, pageable)
                .map(stats -> {

                    Float averagePercentageRead = progressReadingRepository.calculateAveragePercentageReadByChapterId(stats.getChapterId());
                    return ChapterStatsResponse.builder()
                            .chapterId(stats.getChapterId())
                            .title(stats.getTitle())
                            .numberOfViews(stats.getNumberOfViews())
                            .averagePercentageRead(averagePercentageRead != null ? averagePercentageRead : 0f)
                            .build();
                });
    }
}
