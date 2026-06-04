package com.onlinestories.story_service.service;

import com.mongodb.client.result.UpdateResult;
import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.story_service.client.TransactionClient;
import com.onlinestories.story_service.dto.response.ChapterResponse;
import com.onlinestories.story_service.dto.response.FavoriteResponse;
import com.onlinestories.story_service.dto.response.ReadingHistoryResponse;
import com.onlinestories.story_service.dto.response.StoryResponse;
import com.onlinestories.story_service.entity.*;
import com.onlinestories.story_service.enums.ChapterStatus;
import com.onlinestories.story_service.enums.Period;
import com.onlinestories.story_service.enums.StoryStatus;

import com.onlinestories.story_service.repository.*;
import com.onlinestories.story_service.utils.StoryHelper;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReadingService {
    ReadingHistoryRepository readingHistoryRepository;
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    FavoriteRepository favoriteRepository;
    ProgressReadingRepository progressReadingRepository;
    ChapterVersionRepository chapterVersionRepository;
    StoryDailyViewRepository storyDailyViewRepository;
    MongoTemplate mongoTemplate;
    TransactionClient transactionClient;
    StoryHelper storyHelper;

    public Page<ChapterResponse> getChaptersByStoryId(
            String userId, boolean isAdmin, String storyId, int page, int size) {
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
                                .isReadable(chapter.isFreePreview())
                                .createdAt(chapter.getCreatedAt())
                                .lastEditedAt(chapter.getLastEditedAt())
                                .build());
            }
            else if(isAdmin){
                log.info("User {} is an admin fetching all chapters for story {}", userId, storyId);
                chapterPage = chapterRepository.findByStoryIdAndStatus(storyId, ChapterStatus.TAKEN_DOWN, pageable)
                        .map(chapter -> ChapterResponse.builder()
                                .chapterId(chapter.getChapterId())
                                .title(chapter.getTitle())
                                .img(chapter.getImg())
                                .isReadable(chapter.isFreePreview())
                                .status(chapter.getStatus().name())
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
                                .isReadable(chapter.isFreePreview())
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
            Story story = storyRepository.findById(chapter.getStoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

            String content = null;
            if(chapter.getStatus().equals(ChapterStatus.DRAFT)){
                if(!story.getAuthorId().equals(userId)){
                    log.warn("User with ID: {} is not the author of the story and cannot access draft chapter details", userId);
                    throw new AppException(ErrorCode.ACCESS_DENIED);
                }
            }
            else if(chapter.getStatus().equals(ChapterStatus.SCHEDULED)){
                log.info("Chapter {} is scheduled but not published yet, returning details without content", chapterId);
                return ChapterResponse.builder()
                        .chapterId(chapter.getChapterId())
                        .storyId(chapter.getStoryId())
                        .title(chapter.getTitle())
                        .status(chapter.getStatus().name())
                        .img(chapter.getImg())
                        .publishedAt(chapter.getPublishedAt())
                        .build();
            }
            else if(chapter.getStatus().equals(ChapterStatus.PUBLISHED)){
                content = storyHelper.getContentForReading(chapter.getPublishedVersionId());
            }
            else if(chapter.getStatus().equals(ChapterStatus.TAKEN_DOWN)){
                log.warn("Chapter {} has been taken down and cannot be accessed", chapterId);
                throw new AppException(ErrorCode.CHAPTER_IS_TAKEN_DOWN);
            }

            ProgressReading progressReading = progressReadingRepository.findByUserIdAndChapterId(userId, chapterId)
                    .orElse(null);

            return ChapterResponse.builder()
                    .chapterId(chapter.getChapterId())
                    .storyId(chapter.getStoryId())
                    .title(chapter.getTitle())
                    .status(chapter.getStatus().name())
                    .content(content)
                    .img(chapter.getImg())
                    .audioUrl(chapter.getAudioUrl())
                    .percentageRead(progressReading != null ? progressReading.getPercentageRead() : null)
                    .numberOfViews(chapter.getNumberOfViews())
                    .createdAt(chapter.getCreatedAt())
                    .publishedAt(chapter.getPublishedAt())
                    .build();
        }catch (Exception e){
            log.error("Error fetching chapter details: {}", e.getMessage());
            throw e;
        }
    }

    public ChapterResponse getChapterById(String chapterId) {
        log.info("Fetching chapter by ID: {}", chapterId);
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));

        return ChapterResponse.builder()
                .chapterId(chapter.getChapterId())
                .storyId(chapter.getStoryId())
                .title(chapter.getTitle())
                .status(chapter.getStatus().name())
                .img(chapter.getImg())
                .createdAt(chapter.getCreatedAt())
                .publishedAt(chapter.getPublishedAt())
                .lastEditedAt(chapter.getLastEditedAt())
                .build();
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
        LocalDate fromDate = storyHelper.resolveStartTime(effectivePeriod, zone).toLocalDate();
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
        LocalDateTime from = storyHelper.resolveStartTime(effectivePeriod, zone);
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

    public Page<StoryResponse> getRecentlyUpdatedStories(int page, int size) {
        log.info("Getting recently updated stories, page: {}, size: {}", page, size);

        // 1. Chỉ lấy những chapter đã được publish
        var matchPublishedChapters = Aggregation.match(
                Criteria.where("status").is(ChapterStatus.PUBLISHED.name())
        );

        // 2. Sắp xếp chapter theo thời gian publishedAt giảm dần
        var sortChapters = Aggregation.sort(
                Sort.by(Sort.Order.desc("publishedAt"))
        );

        // 3. Group theo storyId, lấy thời gian publishedAt mới nhất
        var groupByStory = Aggregation.group("storyId")
                .first("publishedAt").as("latestPublishedAt");

        // 4. Sắp xếp lại các group theo thời gian latestPublishedAt vừa lấy
        var sortByLatestPublishedAt = Aggregation.sort(
                Sort.by(Sort.Order.desc("latestPublishedAt"))
        );

        // Đổi _id (kết quả của group) thành storyId (string) để dùng cho lookup
        var normalizeKey = Aggregation.project()
                .and("_id").as("storyId")
                .and("latestPublishedAt").as("latestPublishedAt");

        // 5. Lookup sang bảng story để lấy thông tin truyện chi tiết
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

        // 6. Phân trang
        var pageAgg = Aggregation.newAggregation(
                matchPublishedChapters,
                sortChapters,
                groupByStory,
                sortByLatestPublishedAt,
                normalizeKey,
                Aggregation.skip((long) page * size), // Lùi skip/limit lên trước lookup để tối ưu performance
                Aggregation.limit(size),
                lookupStory,
                unwindStory,
                onlyPublishedStories,
                Aggregation.project()
                        .and("storyId").as("storyId")
                        .and("storyDetails.authorId").as("authorId")
                        .and("storyDetails.title").as("title")
                        .and("storyDetails.numberOfChapters").as("numberOfChapters")
                        .and("storyDetails.averageRatingScore").as("averageRatingScore")
                        .and("storyDetails.totalRatingCount").as("totalRatingCount")
                        .and("latestPublishedAt").as("latestModifiedAt")
        );

        AggregationResults<Document> pageResults =
                mongoTemplate.aggregate(pageAgg, "chapter", Document.class);

        var content = pageResults.getMappedResults().stream()
                .map(doc -> StoryResponse.builder()
                        .storyId(doc.getString("storyId"))
                        .authorId(doc.getString("authorId"))
                        .title(doc.getString("title"))
                        .img(doc.getString("img"))
                        .numberOfChapters(doc.get("numberOfChapters") instanceof Number n ? n.intValue() : 0)
                        .averageRatingScore(doc.get("averageRatingScore") instanceof Number n ? n.doubleValue() : 0.0)
                        .totalRatingCount(doc.get("totalRatingCount") instanceof Number n ? n.intValue() : 0)
                        .build())
                .toList();

        // 7. Aggregation để đếm tổng số records
        var countAgg = Aggregation.newAggregation(
                matchPublishedChapters,
                groupByStory
        );

        AggregationResults<Document> countResults =
                mongoTemplate.aggregate(countAgg, "chapter", Document.class);

        long total = countResults.getMappedResults().size();

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }

    public Page<StoryResponse> getAllStories(int page, int size) {
        log.info("Fetching all published stories, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<StoryResponse> storyPage = storyRepository.findByStatusNot(StoryStatus.DRAFT.name(), pageable)
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

        log.info("Fetched {} stories", storyPage.getTotalElements());
        return storyPage;
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

        if(story.getAuthorId().equals(userId) || "GUEST_USER".equals(userId)){
            log.info("User {} is the author of the story or a guest user, skipping view count and history update", userId);
            return ReadingHistoryResponse.builder()
                    .userId(userId)
                    .storyId(storyId)
                    .chapterId(chapterId)
                    .percentageRead(progress)
                    .lastReadAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                    .build();
        }

        LocalDateTime readAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        // view and history for story
        ReadingHistory history = readingHistoryRepository.findByUserIdAndStoryId(userId, storyId)
                .orElseGet(() -> ReadingHistory.builder()
                        .userId(userId)
                        .storyId(storyId)
                        .build());

        boolean shouldIncreaseStoryView = history.getLastView() == null ||
                history.getLastView().plusMinutes(30).isBefore(readAt);

        if (shouldIncreaseStoryView) {
            // story views
            UpdateResult storyUpdate = mongoTemplate.updateFirst(
                    new Query(Criteria.where("_id").is(storyId)),
                    new Update().inc("numberOfViews", 1),
                    Story.class
            );
            log.info("Story view update - matched: {}, modified: {}",
                    storyUpdate.getMatchedCount(), storyUpdate.getModifiedCount());

            UpdateResult dailyStoryUpdate = mongoTemplate.upsert(
                    new Query(Criteria.where("storyId").is(storyId).and("date").is(today)),
                    new Update().inc("viewCount", 1),
                    StoryDailyView.class
            );
            storyHelper.markStoryAsDirty(story.getStoryId());

            log.info("Story daily view update - matched: {}, modified: {}, upsertedId: {}",
                    dailyStoryUpdate.getMatchedCount(), dailyStoryUpdate.getModifiedCount(), dailyStoryUpdate.getUpsertedId());

            history.setLastView(readAt);
        }

        history.setLastChapterId(chapterId);
        history.setLastReadAt(readAt);
        readingHistoryRepository.save(history);


        //view and progress for chapter
        ProgressReading progressReading = progressReadingRepository.findByUserIdAndChapterId(userId, chapterId)
                .orElseGet(() -> ProgressReading.builder()
                        .userId(userId)
                        .chapterId(chapterId)
                        .build());

        boolean shouldIncreaseChapterView = progressReading.getLastView() == null ||
                progressReading.getLastView().plusMinutes(30).isBefore(readAt);

        if (shouldIncreaseChapterView) {
            // chapter views
            UpdateResult chapterUpdate = mongoTemplate.updateFirst(
                    new Query(Criteria.where("_id").is(chapterId)),
                    new Update().inc("numberOfViews", 1),
                    Chapter.class
            );
            progressReading.setLastView(readAt);
            log.info("Chapter view update - matched: {}, modified: {}",
                    chapterUpdate.getMatchedCount(), chapterUpdate.getModifiedCount());
        }

        if (progress != null) {
            log.info("Updating reading progress for user: {}, story: {}, chapter: {} to {}%",
                    userId, storyId, chapterId, progress);
            if(progressReading.getPercentageRead() == null || progress > progressReading.getPercentageRead()){
                progressReading.setPercentageRead(progress);
            }
        }
        progressReading.setLastReadAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        progressReadingRepository.save(progressReading);

        log.info("Updated reading history for user: {}, story: {}, chapter: {}", userId, storyId, chapterId);

        return ReadingHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .userId(userId)
                .storyId(storyId)
                .chapterId(chapterId)
                .percentageRead(progressReading.getPercentageRead())
                .lastReadAt(readAt)
                .build();
    }



    public Page<ReadingHistoryResponse> getReadingHistory(
            String userId, int page, int size){
        try{
            log.info("Fetching reading history for user: {}", userId);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("lastReadAt")));
            Page<ReadingHistory> historyPage = readingHistoryRepository.findByUserId(userId, pageable);

            Page<ReadingHistoryResponse> responsePage = historyPage.map(history -> {
                Float percentage = null;
                if (history.getLastChapterId() != null) {
                    percentage = progressReadingRepository
                            .findByUserIdAndChapterId(userId, history.getLastChapterId())
                            .map(ProgressReading::getPercentageRead)
                            .orElse(null);
                }

                return ReadingHistoryResponse.builder()
                        .historyId(history.getHistoryId())
                        .userId(history.getUserId())
                        .storyId(history.getStoryId())
                        .chapterId(history.getLastChapterId())
                        .percentageRead(percentage)
                        .lastReadAt(history.getLastReadAt())
                        .build();
            });

            log.info("Reading history fetched successfully, total records: {}", responsePage.getTotalElements());
            return responsePage;
        }
        catch(Exception e){
            log.error("Error fetching reading history: {}", e.getMessage());
            throw e;
        }
    }

    public ReadingHistoryResponse getLatestChapter(
            String userId, String storyId){
        log.info("Fetching latest chapter read for user: {}, story: {}", userId, storyId);
        return readingHistoryRepository.findByUserIdAndStoryId(userId, storyId)
                .map(history -> {
                    // Get percentage read for the last chapter if available
                    Float percentage = null;
                    if (history.getLastChapterId() != null) {
                        percentage = progressReadingRepository
                                .findByUserIdAndChapterId(userId, history.getLastChapterId())
                                .map(ProgressReading::getPercentageRead)
                                .orElse(null);
                    }


                    return ReadingHistoryResponse.builder()
                            .historyId(history.getHistoryId())
                            .chapterId(history.getLastChapterId())
                            .percentageRead(percentage)
                            .build();
                })
                .orElse(null);
    }

    public Page<FavoriteResponse> getFavoriteStories(String userId, int page, int size){
        log.info("Fetching favorite stories for user: {}, page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Favorite> favoritePage = favoriteRepository.findByUserId(userId, pageable);

        return favoritePage.map(favorite -> FavoriteResponse.builder()
                .storyId(favorite.getStoryId())
                .build());
    }

    public boolean isUnlockStory(String userId, String storyId){
        log.info("Checking if story {} is unlocked for user {}", storyId, userId);
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));
        if ("GUEST_USER".equals(userId)) {
            return false;
        }
        return story.getAuthorId().equals(userId) || transactionClient.checkIfStoryUnlocked(userId, storyId);
    }



}
