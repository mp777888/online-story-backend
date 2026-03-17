package com.onlinestories.story_service.Service;

import com.onlinestories.story_service.DTO.Response.ChapterResponse;
import com.onlinestories.story_service.DTO.Response.ReadingHistoryResponse;
import com.onlinestories.story_service.DTO.Response.StoryResponse;
import com.onlinestories.story_service.DTO.Response.VersionResponse;
import com.onlinestories.story_service.Entity.ChapterVersion;
import com.onlinestories.story_service.Entity.ReadingHistory;
import com.onlinestories.story_service.Entity.Story;
import com.onlinestories.story_service.Enum.ChapterStatus;
import com.onlinestories.story_service.Enum.StoryStatus;
import com.onlinestories.story_service.Repository.ChapterRepository;
import com.onlinestories.story_service.Repository.ChapterVersionRepository;
import com.onlinestories.story_service.Repository.ReadingHistoryRepository;
import com.onlinestories.story_service.Repository.StoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public ResponseEntity<Page<ChapterResponse>> getChaptersByStoryId(
            String storyId, int page, int size) {
        try{
            log.info("Fetching chapters for story: {}, page: {}, size: {}", storyId, page, size);
            if(!storyRepository.existsById(storyId)){
                log.error("Story with ID {} not found", storyId);
                return ResponseEntity.notFound().build();
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<ChapterResponse> chapterPage = chapterRepository.findByStoryIdAndStatus(storyId, ChapterStatus.PUBLISHED, pageable)
                    .map(chapter -> ChapterResponse.builder()
                            .chapterId(chapter.getChapterId())
                            .title(chapter.getTitle())
                            .createdAt(chapter.getCreatedAt())
                            .build());

            log.info("Fetched {} chapters for story {}", chapterPage.getTotalElements(), storyId);
            return ResponseEntity.ok().body(chapterPage);
        }
        catch (Exception ex){
            log.error("Error fetching chapters for story {}: {}", storyId, ex.getMessage(), ex);
            throw ex;
        }
    }

    public ResponseEntity<Page<StoryResponse>> getUserStories(
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
            return ResponseEntity.ok().body(storyPage);

        }
        catch(Exception ex){
            log.error("Error fetching stories for user {}: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    public ResponseEntity<VersionResponse> getContentForReading(String chapterId) {
        try{
            log.info("Fetching chapter version details for chapterId: {}", chapterId);
            ChapterVersion version = chapterVersionRepository.findByChapterIdAndIsPublished(chapterId);

            if(version == null){
                log.warn("Published chapter version not found for chapterId: {}", chapterId);
                return ResponseEntity.status(404).build();
            }

            log.info("Chapter version details fetched successfully for chapterId: {}", chapterId);
            return ResponseEntity.ok().body(VersionResponse.builder()
                    .versionId(version.getChapterVersionId())
                    .chapterId(version.getChapterId())
                    .versionName(version.getVersionName())
                    .content(version.getContent())
                    .createdAt(version.getCreatedAt())
                    .build());
        } catch (Exception e){
            log.error("Error fetching chapter version details: {}", e.getMessage());
            throw e;
        }
    }


    public ResponseEntity<String> addToReadingHistory(String userId, String storyId, String chapterId){
        try{
            log.info("Processing reading history for user: {}, story: {}", userId, storyId);

            // Tìm lịch sử đọc cũ của truyện này cho user này
            ReadingHistory history = readingHistoryRepository
                    .findByUserIdAndStoryId(userId, storyId)
                    .map(existingHistory -> {
                        // Cập nhật chapterId và lastReadAt nếu đã tồn tại
                        existingHistory.setChapterId(chapterId);
                        existingHistory.setLastReadAt(java.time.LocalDateTime.now());
                        return existingHistory;
                    })
                    .orElseGet(() -> {
                        // Nếu chưa có, tạo record mới hoàn toàn
                        return ReadingHistory.builder()
                                .userId(userId)
                                .storyId(storyId)
                                .chapterId(chapterId)
                                .lastReadAt(java.time.LocalDateTime.now())
                                .build();
                    });

            readingHistoryRepository.save(history);

            log.info("Reading history updated successfully");
            return ResponseEntity.ok().body("History updated");
        }
        catch(Exception e){
            log.error("Error adding to reading history: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<Page<ReadingHistoryResponse>> getReadingHistory(
            String userId, int page, int size){
        try{
            log.info("Fetching reading history for user: {}", userId);
            Pageable pageable = PageRequest.of(page, size);
            Page<ReadingHistoryResponse> historyPage = readingHistoryRepository
                    .findByUserId(userId, pageable)
                    .map(history -> {
                        String chapterName = chapterRepository.findById(history.getChapterId())
                                .map(chapter -> chapter.getTitle())
                                .orElse("Unknown Chapter");
                        String storyName = storyRepository.findById(history.getStoryId())
                                .map(story -> story.getTitle())
                                .orElse("Unknown Story");

                        return ReadingHistoryResponse.builder()
                                .historyId(history.getHistoryId())
                                .userId(history.getUserId())
                                .storyName(storyName)
                                .chapterName(chapterName)
                                .lastReadAt(history.getLastReadAt())
                                .build();
                    });
            log.info("Reading history fetched successfully, total records: {}", historyPage.getTotalElements());
            return ResponseEntity.ok().body(historyPage);
        }
        catch(Exception e){
            log.error("Error fetching reading history: {}", e.getMessage());
            throw e;
        }
    }
}
