package com.onlinestories.story_service.Service;

import com.onlinestories.story_service.DTO.Response.ChapterResponse;
import com.onlinestories.story_service.DTO.Response.ReadingHistoryResponse;
import com.onlinestories.story_service.DTO.Response.StoryResponse;
import com.onlinestories.story_service.DTO.Response.VersionResponse;
import com.onlinestories.story_service.Entity.*;
import com.onlinestories.story_service.Enum.ChapterStatus;
import com.onlinestories.story_service.Enum.StoryStatus;
import com.onlinestories.story_service.Exception.AppException;
import com.onlinestories.story_service.Exception.ErrorCode;
import com.onlinestories.story_service.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    MongoTemplate mongoTemplate;

    public Page<ChapterResponse> getChaptersByStoryId(
            String userId, String storyId, int page, int size) {
        try{
            log.info("Fetching chapters for story: {}, page: {}, size: {}", storyId, page, size);

            Story story = storyRepository.findById(storyId)
                    .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));


            if(story.getStatus().equals(StoryStatus.DRAFT) && !story.getAuthorId().equals(userId)){
                log.warn("Story {} is not published. Current status: {}", storyId, story.getStatus());
                throw new AppException(ErrorCode.NOT_AUTHOR_OF_STORY);
            }


            Pageable pageable = PageRequest.of(page, size);
            Page<ChapterResponse> chapterPage = chapterRepository.findByStoryIdAndStatus(storyId, ChapterStatus.PUBLISHED, pageable)
                    .map(chapter -> ChapterResponse.builder()
                            .chapterId(chapter.getChapterId())
                            .title(chapter.getTitle())
                            .img(chapter.getImg())
                            .createdAt(chapter.getCreatedAt())
                            .build());

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

    @Transactional
    public ReadingHistoryResponse readChapter(String userId, String storyId, String chapterId) {
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

        ReadingHistory history = readingHistoryRepository.findByUserIdAndStoryId(userId, storyId)
                .orElse(null);

        boolean shouldIncreaseView = false;
        LocalDateTime readAt = LocalDateTime.now();

        if (history == null) {
            history = ReadingHistory.builder()
                    .userId(userId)
                    .storyId(storyId)
                    .lastView(readAt)
                    .build();
            shouldIncreaseView = true;
        } else {
            if (history.getLastView() == null || history.getLastView().plusMinutes(30).isBefore(readAt)) {
                shouldIncreaseView = true;
                history.setLastView(readAt);
            }
        }

        if (shouldIncreaseView) {
            Query query = new Query().addCriteria(Criteria.where("storyId").is(storyId));
            Update update = new Update().inc("views", 1);
            mongoTemplate.updateFirst(query, update, Story.class);
            log.info("Increased view count for story: {}", storyId);
        }

        history.setChapterId(chapterId);
        history.setLastReadAt(readAt);
        readingHistoryRepository.save(history);

        log.info("Updated reading history for user: {}, story: {}, chapter: {}", userId, storyId, chapterId);

        return ReadingHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .userId(userId)
                .storyId(storyId)
                .chapterId(chapterId)
                .lastReadAt(readAt)
                .build();
    }

    public VersionResponse getContentForReading(String chapterId) {
        try{
            log.info("Fetching chapter version details for chapterId: {}", chapterId);
            ChapterVersion version = chapterVersionRepository.findByChapterIdAndIsPublishedTrue(chapterId);

            if(version == null){
                log.warn("Published chapter version not found for chapterId: {}", chapterId);
                throw new AppException(ErrorCode.VERSION_NOT_FOUND);
            }

            log.info("Chapter version details fetched successfully for chapterId: {}", chapterId);
            return VersionResponse.builder()
                    .versionId(version.getChapterVersionId())
                    .chapterId(version.getChapterId())
                    .versionName(version.getVersionName())
                    .content(version.getContent())
                    .createdAt(version.getCreatedAt())
                    .build();
        } catch (Exception e){
            log.error("Error fetching chapter version details: {}", e.getMessage());
            throw e;
        }
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
                            .chapterId(history.getChapterId())
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
}
