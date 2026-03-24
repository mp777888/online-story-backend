package com.onlinestories.story_service.Service;

import com.mongodb.client.result.UpdateResult;
import com.onlinestories.story_service.DTO.Response.ChapterResponse;
import com.onlinestories.story_service.DTO.Response.ReadingHistoryResponse;
import com.onlinestories.story_service.DTO.Response.StoryResponse;
import com.onlinestories.story_service.Entity.*;
import com.onlinestories.story_service.Enum.ChapterStatus;
import com.onlinestories.story_service.Enum.Period;
import com.onlinestories.story_service.Enum.StoryStatus;
import com.onlinestories.story_service.Exception.AppException;
import com.onlinestories.story_service.Exception.ErrorCode;
import com.onlinestories.story_service.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReadingService {
    ReadingHistoryRepository readingHistoryRepository;
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    ChapterVersionRepository chapterVersionRepository;
    StoryDailyViewRepository storyDailyViewRepository;
    MongoTemplate mongoTemplate;

    public Page<ChapterResponse> getChaptersByStoryId(
            String userId, String storyId, int page, int size) {
        try{
            log.info("Fetching chapters for story: {}, page: {}, size: {}", storyId, page, size);

            Story story = storyRepository.findById(storyId)
                    .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

            Pageable pageable = PageRequest.of(page, size);
            Page<ChapterResponse> chapterPage;
            if(story.getAuthorId().equals(userId)) {
                log.info("User {} is the author of story {}, fetching all chapters including drafts", userId, storyId);
                chapterPage = chapterRepository.findByStoryId(storyId, pageable)
                        .map(chapter -> ChapterResponse.builder()
                                .chapterId(chapter.getChapterId())
                                .title(chapter.getTitle())
                                .img(chapter.getImg())
                                .createdAt(chapter.getCreatedAt())
                                .build());
            }
            else{
                log.info("User {} is not the author of story {}, fetching only published chapters", userId, storyId);
                chapterPage = chapterRepository.findByStoryIdAndStatus(storyId, ChapterStatus.PUBLISHED, pageable)
                        .map(chapter -> ChapterResponse.builder()
                                .chapterId(chapter.getChapterId())
                                .title(chapter.getTitle())
                                .img(chapter.getImg())
                                .createdAt(chapter.getCreatedAt())
                                .build());
            }
            log.info("Fetched {} chapters for story {}", chapterPage.getTotalElements(), storyId);
            return chapterPage;
        }
        catch (Exception ex){
            log.error("Error fetching chapters for story {}: {}", storyId, ex.getMessage(), ex);
            throw ex;
        }
    }

    public Page<StoryResponse> getUserStories(
            String userId, int page, int size) {
        try{
            log.info("Fetching stories for user: {}, page: {}, size: {}", userId, page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<StoryResponse> storyPage = storyRepository.findByAuthorIdAndStatusNot(userId, StoryStatus.DRAFT, pageable)
                    .map(story -> StoryResponse.builder()
                            .storyId(story.getStoryId())
                            .authorId(story.getAuthorId())
                            .title(story.getTitle())
                            .description(story.getDescription())
                            .img(story.getImg())
                            .status(story.getStatus().name())
                            .numberOfChapters(story.getNumberOfChapters())
                            .numberOfViews(story.getNumberOfViews())
                            .averageRatingScore(story.getAverageRatingScore())
                            .totalRatingCount(story.getTotalRatingCount())
                            .genres(story.getGenres()
                                    .stream()
                                    .map(Story.GenreSummary::getName)
                                    .collect(Collectors.toSet()))
                            .build());

            log.info("Fetched {} stories for user {}", storyPage.getTotalElements(), userId);
            return storyPage;

        }
        catch(Exception ex){
            log.error("Error fetching stories for user {}: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    // Get chapter details
    public ChapterResponse getChapterDetails(String userId, String chapterId) {
        try {
            log.info("Fetching chapter details for chapterId: {}", chapterId);
            Chapter chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));

            String content = null;
            if(!chapter.getStatus().equals(ChapterStatus.PUBLISHED)){
                Story story = storyRepository.findById(chapter.getStoryId())
                        .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));
                if(!story.getAuthorId().equals(userId)){
                    log.warn("User with ID: {} is not the author of the story and cannot access draft chapter details", userId);
                    throw new AppException(ErrorCode.ACCESS_DENIED);
                }
            }
            else {
                content = getContentForReading(chapterId);
            }

            return ChapterResponse.builder()
                    .chapterId(chapter.getChapterId())
                    .storyId(chapter.getStoryId())
                    .title(chapter.getTitle())
                    .status(chapter.getStatus().name())
                    .content(content)
                    .img(chapter.getImg())
                    .audioUrl(chapter.getAudioUrl())
                    .createdAt(chapter.getCreatedAt())
                    .publishedAt(chapter.getPublishedAt())
                    .build();
        }catch (Exception e){
            log.error("Error fetching chapter details: {}", e.getMessage());
            throw e;
        }
    }

    public Page<StoryResponse> getTopViewedStories(Period period, int page, int size) {
        log.info("Getting top viewed stories for period: {}, page: {}, size: {}", period, page, size);

        Period effectivePeriod = (period == null) ? Period.ALL_TIME : period;

        // ALL_TIME: đọc trực tiếp từ Story
        if (effectivePeriod == Period.ALL_TIME) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("numberOfViews"), Sort.Order.asc("storyId")));
            Page<Story> stories = storyRepository.findByStatusNotAndNumberOfViewsGreaterThan(
                    StoryStatus.DRAFT, 0, pageable
            );

            return stories.map(story -> StoryResponse.builder()
                    .storyId(story.getStoryId())
                    .title(story.getTitle())
                    .authorId(story.getAuthorId())
                    .numberOfChapters(story.getNumberOfChapters())
                    .numberOfViews(story.getNumberOfViews())
                    .build());
        }

        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate fromDate = resolveStartTime(effectivePeriod, zone).toLocalDate();
        LocalDate toDate = LocalDate.now(zone);

        var dateMatch = Aggregation.match(
                Criteria.where("date").gte(fromDate).lte(toDate)
        );

        var groupByStory = Aggregation.group("storyId")
                .sum("viewCount").as("periodViews");

        // Đổi _id -> storyId (string) trước khi lookup
        var normalizeKey = Aggregation.project()
                .and("_id").as("storyId")
                .and("periodViews").as("periodViews");

        // Lookup bằng cách so sánh string của story._id với storyId
        AggregationOperation lookupStory = context -> new Document("$lookup",
                new Document("from", "story")
                        .append("let", new Document("sid", "$storyId"))
                        .append("pipeline", List.of(
                                new Document("$match", new Document("$expr",
                                        new Document("$eq", List.of(
                                                new Document("$toString", "$_id"),
                                                "$$sid"
                                        ))
                                ))
                        ))
                        .append("as", "storyDetails")
        );

        var unwindStory = Aggregation.unwind("storyDetails");

        var onlyPublishedStories = Aggregation.match(
                Criteria.where("storyDetails.status").ne(StoryStatus.DRAFT.name())
        );

        var sortByViews = Aggregation.sort(
                Sort.by(Sort.Order.desc("periodViews"), Sort.Order.asc("storyId"))
        );

        var pageAgg = Aggregation.newAggregation(
                dateMatch,
                groupByStory,
                normalizeKey,
                lookupStory,
                unwindStory,
                onlyPublishedStories,
                sortByViews,
                Aggregation.skip((long) page * size),
                Aggregation.limit(size),
                Aggregation.project()
                        .and("storyId").as("storyId")
                        .and("storyDetails.title").as("title")
                        .and("storyDetails.authorId").as("authorId")
                        .and("storyDetails.numberOfChapters").as("numberOfChapters")
                        .and("periodViews").as("numberOfViews")
        );

        AggregationResults<Document> pageResults =
                mongoTemplate.aggregate(pageAgg, "storyDailyView", Document.class);

        var content = pageResults.getMappedResults().stream()
                .map(doc -> StoryResponse.builder()
                        .storyId(doc.getString("storyId"))
                        .authorId(doc.getString("authorId"))
                        .title(doc.getString("title"))
                        .numberOfChapters(doc.get("numberOfChapters") instanceof Number n ? n.intValue() : 0)
                        .numberOfViews(doc.get("numberOfViews") instanceof Number n ? n.intValue() : 0)
                        .build())
                .toList();

        var countAgg = Aggregation.newAggregation(
                dateMatch,
                groupByStory,
                normalizeKey,
                lookupStory,
                unwindStory,
                onlyPublishedStories,
                Aggregation.count().as("total")
        );

        AggregationResults<Document> countResults =
                mongoTemplate.aggregate(countAgg, "storyDailyView", Document.class);

        long total = countResults.getMappedResults().isEmpty()
                ? 0L
                : countResults.getMappedResults().get(0).get("total") instanceof Number n ? n.longValue() : 0L;

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }


    public Page<StoryResponse> getTopRatingStories(Period period, int page, int size) {
        log.info("Getting top rating stories for period: {}, page: {}, size: {}", period, page, size);

        Period effectivePeriod = (period == null) ? Period.ALL_TIME : period;

        // ALL_TIME: đọc trực tiếp từ Story
        if (effectivePeriod == Period.ALL_TIME) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("averageRatingScore"), Sort.Order.desc("totalRatingCount"), Sort.Order.asc("storyId")));
            Page<Story> stories = storyRepository.findByStatusNotAndAverageRatingScoreGreaterThan(
                    StoryStatus.DRAFT, 0.0, pageable
            );

            return stories.map(story -> StoryResponse.builder()
                    .storyId(story.getStoryId())
                    .title(story.getTitle())
                    .authorId(story.getAuthorId())
                    .numberOfChapters(story.getNumberOfChapters())
                    .averageRatingScore(story.getAverageRatingScore())
                    .totalRatingCount(story.getTotalRatingCount())
                    .build());
        }

        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime from = resolveStartTime(effectivePeriod, zone);
        LocalDateTime to = LocalDateTime.now(zone);

        var dateMatch = Aggregation.match(
                Criteria.where("ratedAt").gte(from).lt(to)
        );

        var groupByStory = Aggregation.group("storyId")
                .avg("ratingScore").as("averageRatingScore")
                .count().as("totalRatingCount");

        var normalizeKey = Aggregation.project()
                .and("_id").as("storyId")
                .and("averageRatingScore").as("averageRatingScore")
                .and("totalRatingCount").as("totalRatingCount");

        AggregationOperation lookupStory = context -> new Document("$lookup",
                new Document("from", "story")
                        .append("let", new Document("sid", "$storyId"))
                        .append("pipeline", List.of(
                                new Document("$match", new Document("$expr",
                                        new Document("$eq", List.of(
                                                new Document("$toString", "$_id"),
                                                "$$sid"
                                        ))
                                ))
                        ))
                        .append("as", "story")
        );

        var unwindStory = Aggregation.unwind("story");

        var onlyPublishedStories = Aggregation.match(
                Criteria.where("story.status").ne(StoryStatus.DRAFT.name())
        );

        var sortByScore = Aggregation.sort(
                Sort.by(
                        Sort.Order.desc("averageRatingScore"),
                        Sort.Order.desc("totalRatingCount"),
                        Sort.Order.asc("storyId")
                )
        );

        var pageAgg = Aggregation.newAggregation(
                dateMatch,
                groupByStory,
                normalizeKey,
                lookupStory,
                unwindStory,
                onlyPublishedStories,
                sortByScore,
                Aggregation.skip((long) page * size),
                Aggregation.limit(size),
                Aggregation.project()
                        .and("storyId").as("storyId")
                        .and("story.authorId").as("authorId")
                        .and("story.title").as("title")
                        .and("story.numberOfChapters").as("numberOfChapters")
                        .and("averageRatingScore").as("averageRatingScore")
                        .and("totalRatingCount").as("totalRatingCount")
        );

        AggregationResults<Document> pageResults =
                mongoTemplate.aggregate(pageAgg, "rating", Document.class);

        var content = pageResults.getMappedResults().stream()
                .map(doc -> StoryResponse.builder()
                        .storyId(doc.getString("storyId"))
                        .authorId(doc.getString("authorId"))
                        .title(doc.getString("title"))
                        .numberOfChapters(doc.get("numberOfChapters") instanceof Number n ? n.intValue() : 0)
                        .averageRatingScore(doc.get("averageRatingScore") instanceof Number n ? n.doubleValue() : 0.0)
                        .totalRatingCount(doc.get("totalRatingCount") instanceof Number n ? n.intValue() : 0)
                        .build())
                .toList();

        var countAgg = Aggregation.newAggregation(
                dateMatch,
                groupByStory,
                normalizeKey,
                lookupStory,
                unwindStory,
                onlyPublishedStories,
                Aggregation.count().as("total")
        );

        AggregationResults<Document> countResults =
                mongoTemplate.aggregate(countAgg, "rating", Document.class);

        long total = countResults.getMappedResults().isEmpty()
                ? 0L
                : countResults.getMappedResults().get(0).get("total") instanceof Number n ? n.longValue() : 0L;

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }


    @Transactional
    public ReadingHistoryResponse readChapter(String userId, String storyId, String chapterId, Float progress) {
        log.info("Processing reading chapter for user: {}, story: {}, chapter: {}", userId, storyId, chapterId);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with ID: " + storyId));

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found with ID: " + chapterId));

        if(story.getStatus().equals(StoryStatus.DRAFT)){
            log.warn("Story {} is not published. Current status: {}", storyId, story.getStatus());
            throw new AppException(ErrorCode.STORY_IS_NOT_PUBLISHED);
        }

        if(chapter.getStatus() != ChapterStatus.PUBLISHED){
            log.warn("Chapter {} is not published. Current status: {}", chapterId, chapter.getStatus());
            throw new AppException(ErrorCode.CHAPTER_IS_NOT_PUBLISHED);
        }

        if(story.getAuthorId().equals(userId)){
            log.info("User {} is the author of the story {}, allowing access to read chapter without view count increment", userId, storyId);
            return ReadingHistoryResponse.builder()
                    .userId(userId)
                    .storyId(storyId)
                    .chapterId(chapterId)
                    .percentageRead(progress)
                    .lastReadAt(LocalDateTime.now())
                    .build();
        }

        ReadingHistory history = readingHistoryRepository.findByUserIdAndStoryId(userId, storyId)
                .orElseGet(() -> ReadingHistory.builder()
                        .userId(userId)
                        .storyId(storyId)
                        .build());

        LocalDateTime readAt = LocalDateTime.now();

        boolean shouldIncreaseStoryView = history.getLastView() == null ||
                history.getLastView().plusMinutes(30).isBefore(readAt);

        Map<String, LocalDateTime> viewTimes = history.getChapterViewTimes();
        if (viewTimes == null) {
            viewTimes = new HashMap<>();
        }

        LocalDateTime lastChapterView = viewTimes.get(chapterId);
        boolean shouldIncreaseChapterView = lastChapterView == null ||
                lastChapterView.plusMinutes(30).isBefore(readAt);



        if (shouldIncreaseStoryView) {
            // story views
            UpdateResult storyUpdate = mongoTemplate.updateFirst(
                    new Query(Criteria.where("_id").is(storyId)),
                    new Update().inc("numberOfViews", 1),
                    Story.class
            );
            log.info("Story view update - matched: {}, modified: {}",
                    storyUpdate.getMatchedCount(), storyUpdate.getModifiedCount());
            history.setLastView(readAt);
        }

        if (shouldIncreaseChapterView) {
            // chapter views
            UpdateResult chapterUpdate = mongoTemplate.updateFirst(
                    new Query(Criteria.where("_id").is(chapterId)),
                    new Update().inc("numberOfViews", 1),
                    Chapter.class
            );
            log.info("Chapter view update - matched: {}, modified: {}",
                    chapterUpdate.getMatchedCount(), chapterUpdate.getModifiedCount());
            viewTimes.put(chapterId, readAt);
            history.setChapterViewTimes(viewTimes);
        }

        history.setLastChapterId(chapterId);
        history.setLastReadAt(readAt);
        if (progress != null) {
            log.info("Updating reading progress for user: {}, story: {}, chapter: {} to {}%",
                    userId, storyId, chapterId, progress);
            history.setPercentageRead(progress);
        }
        readingHistoryRepository.save(history);

        log.info("Updated reading history for user: {}, story: {}, chapter: {}", userId, storyId, chapterId);

        return ReadingHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .userId(userId)
                .storyId(storyId)
                .chapterId(chapterId)
                .percentageRead(history.getPercentageRead())
                .lastReadAt(readAt)
                .build();
    }

    private String getContentForReading(String chapterId) {
        log.info("Fetching chapter version details for chapterId: {}", chapterId);
        ChapterVersion version = chapterVersionRepository.findByChapterIdAndIsPublishedTrue(chapterId);

        if(version == null){
            log.warn("Published chapter version not found for chapterId: {}", chapterId);
            throw new AppException(ErrorCode.VERSION_NOT_FOUND);
        }

        log.info("Chapter version details fetched successfully for chapterId: {}", chapterId);
        return version.getContent();
    }

    public Page<ReadingHistoryResponse> getReadingHistory(
            String userId, int page, int size){
        try{
            log.info("Fetching reading history for user: {}", userId);


            Pageable pageable = PageRequest.of(page, size);
            Page<ReadingHistoryResponse> historyPage = readingHistoryRepository
                    .findByUserId(userId, pageable)
                    .map(history -> ReadingHistoryResponse.builder()
                            .historyId(history.getHistoryId())
                            .userId(history.getUserId())
                            .storyId(history.getStoryId())
                            .chapterId(history.getLastChapterId())
                            .percentageRead(history.getPercentageRead())
                            .lastReadAt(history.getLastReadAt())
                            .build());
            log.info("Reading history fetched successfully, total records: {}", historyPage.getTotalElements());
            return historyPage;
        }
        catch(Exception e){
            log.error("Error fetching reading history: {}", e.getMessage());
            throw e;
        }
    }

    private LocalDateTime resolveStartTime(Period period, ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);
        return switch (period) {
            case TODAY -> today.atStartOfDay();
            case WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
            case MONTH -> today.withDayOfMonth(1).atStartOfDay();
            case ALL_TIME -> LocalDateTime.MIN;
        };
    }

}
