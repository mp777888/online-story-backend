package com.onlinestories.story_service.Service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.story_service.DTO.Response.ChapterStatsResponse;
import com.onlinestories.story_service.DTO.Response.StoryDailyViewResponse;
import com.onlinestories.story_service.DTO.Response.TrendStatisticResponse;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AnalyticsService {
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    ProgressReadingRepository progressReadingRepository;
    StoryDailyViewRepository storyDailyViewRepository;
    MongoTemplate mongoTemplate;


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

    /**     * Lấy Top Categories (thể loại) xu hướng     */
    public List<TrendStatisticResponse> getTopTrendingCategories(Instant startDate, Instant endDate, int limit) {
        log.info("Fetching top trending categories from {} to {}", startDate, endDate);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(startDate).lte(endDate)),

                Aggregation.unwind("genres"),

                // Trỏ tới trường name bên trong GenreSummary Object
                Aggregation.group("genres.name").count().as("count"),

                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(limit),
                Aggregation.project("count").and("_id").as("name")
        );

        AggregationResults<TrendStatisticResponse> results = mongoTemplate.aggregate(
                aggregation, Story.class, TrendStatisticResponse.class);

        return results.getMappedResults();
    }

    /**     * Lấy Top Tags xu hướng     */
    public List<TrendStatisticResponse> getTopTrendingTags(Instant startDate, Instant endDate, int limit) {
        log.info("Fetching top trending tags from {} to {}", startDate, endDate);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(startDate).lte(endDate)),

                Aggregation.unwind("tags"),
                Aggregation.group("tags").count().as("count"),

                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(limit),
                Aggregation.project("count").and("_id").as("name")
        );

        AggregationResults<TrendStatisticResponse> results = mongoTemplate.aggregate(
                aggregation, Story.class, TrendStatisticResponse.class);

        return results.getMappedResults();
    }


}
