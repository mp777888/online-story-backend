package com.onlinestories.story_service.Service;

import com.onlinestories.story_service.DTO.Response.ReadingHistoryResponse;
import com.onlinestories.story_service.Entity.ReadingHistory;
import com.onlinestories.story_service.Repository.ChapterRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReadingHistoryService {
    ReadingHistoryRepository readingHistoryRepository;
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;

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
