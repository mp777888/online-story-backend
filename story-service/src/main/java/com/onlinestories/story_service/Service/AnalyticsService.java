package com.onlinestories.story_service.Service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.story_service.DTO.Response.AuthorMonthlyStatsResponse;
import com.onlinestories.story_service.DTO.Response.ChapterStatsResponse;
import com.onlinestories.story_service.DTO.Response.StoryDailyViewResponse;
import com.onlinestories.story_service.DTO.Response.TrendStatisticResponse;
import com.onlinestories.story_service.Entity.Chapter;
import com.onlinestories.story_service.Entity.Story;
import com.onlinestories.story_service.Entity.StoryDailyView;

import com.onlinestories.story_service.Enum.ChapterStatus;
import com.onlinestories.story_service.Enum.StoryStatus;
import com.onlinestories.story_service.Repository.ChapterRepository;
import com.onlinestories.story_service.Repository.ProgressReadingRepository;
import com.onlinestories.story_service.Repository.StoryDailyViewRepository;
import com.onlinestories.story_service.Repository.StoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
            String userId, String storyId, String monthStr, int page, int size) {

        log.info("Fetching daily views for story ID: {}, month: {}, page: {}, size: {}", storyId, monthStr, page, size);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

        if (!story.getAuthorId().equals(userId)) {
            log.warn("Unauthorized access attempt by user ID: {} for story ID: {}", userId, storyId);
            throw new AppException(ErrorCode.NOT_AUTHOR_OF_STORY);
        }

        // Ưu tiên sắp xếp ngày mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<StoryDailyView> storyDailyViews;

        if (monthStr != null && !monthStr.trim().isEmpty()) {
            // Lấy khoảng thời gian của tháng
            YearMonth yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            storyDailyViews = storyDailyViewRepository.findByStoryIdAndDateBetween(storyId, startDate, endDate, pageable);
        } else {
            storyDailyViews = storyDailyViewRepository.findByStoryId(storyId, pageable);
        }

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

    // Thêm hàm này vào AnalyticsService.java

    public AuthorMonthlyStatsResponse getAuthorMonthlyStats(String authorId, String monthStr) {
        log.info("Fetching monthly stats for author: {}, month: {}", authorId, monthStr);

        // 1. Phân tích thời gian (Parse month string to dates)
        YearMonth yearMonth = java.time.YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Criteria dateCriteria = Criteria.where("date").gte(startDate).lte(endDate);

        // 2. Lấy danh sách câu truyện của tác giả này
        List<Story> authorStories = storyRepository.findByAuthorIdAndStatusNot(
                        authorId, com.onlinestories.story_service.Enum.StoryStatus.DRAFT, PageRequest.of(0, 9999))
                .toList();

        List<String> authorStoryIds = authorStories.stream()
                .map(Story::getStoryId)
                .toList();

        if (authorStoryIds.isEmpty()) {
            return AuthorMonthlyStatsResponse.builder()
                    .authorId(authorId).month(monthStr)
                    .totalViews(0).totalPublishedChapters(0)
                    .estimatedIncome(0)
                    .topStories(List.of())
                    .build();
        }

        Criteria authorStoryCriteria = Criteria.where("storyId").in(authorStoryIds);

        // 3. Tính: Tổng Views trong tháng và Top Story View
        Aggregation viewAggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(dateCriteria, authorStoryCriteria)),
                Aggregation.group("storyId").sum("viewCount").as("periodViews"),
                Aggregation.sort(Sort.Direction.DESC, "periodViews")
        );

        AggregationResults<org.bson.Document> viewResults = mongoTemplate.aggregate(
                viewAggregation, "storyDailyView", org.bson.Document.class);

        long totalMonthViews = 0;
        List<AuthorMonthlyStatsResponse.TopStoryStat> topStories = new java.util.ArrayList<>();

        // Quét kết quả Aggregation views
        for (Document doc : viewResults.getMappedResults()) {
            String sId = doc.getString("_id");
            Number views = (Number) doc.get("periodViews");
            long vCount = views != null ? views.longValue() : 0;

            totalMonthViews += vCount;

            // Nếu muốn lấy Top 5 truyện xem nhiều nhất
            if (topStories.size() < 5) {
                // Lấy thông tin truyện (Title, Chapters)
                storyRepository.findById(sId).ifPresent(s -> topStories.add(AuthorMonthlyStatsResponse.TopStoryStat.builder()
                        .storyId(sId)
                        .title(s.getTitle())
                        .numberOfChapters(s.getNumberOfChapters())
                        .periodViews(vCount)
                        .build()));
            }
        }

        // 4. Tính: Tổng số chương đã đăng tải (PUBLISHED) TRONG THÁNG NÀY
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999);

        Query chapterQuery = new Query(
                new Criteria().andOperator(
                        Criteria.where("storyId").in(authorStoryIds), // Các truyện của tác giả
                        Criteria.where("status").is(ChapterStatus.PUBLISHED), // Trạng thái đã đăng
                        Criteria.where("publishedAt").gte(startDateTime).lte(endDateTime) // Đăng trong khoảng thời gian này
                )
        );

        long totalPublishedChaptersThisMonth = mongoTemplate.count(chapterQuery, Chapter.class);

        // 5. Trả về kết quả
        return AuthorMonthlyStatsResponse.builder()
                .authorId(authorId)
                .month(monthStr)
                .totalViews(totalMonthViews)
                .totalPublishedChapters(totalPublishedChaptersThisMonth)
                .estimatedIncome(totalMonthViews * 0.5)
                .topStories(topStories)
                .build();
    }
}
