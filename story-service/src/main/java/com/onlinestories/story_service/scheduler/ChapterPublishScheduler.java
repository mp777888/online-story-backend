package com.onlinestories.story_service.scheduler;

import com.onlinestories.story_service.entity.Chapter;
import com.onlinestories.story_service.entity.ChapterVersion;
import com.onlinestories.story_service.entity.Story;
import com.onlinestories.story_service.enums.ChapterStatus;
import com.onlinestories.story_service.repository.ChapterRepository;
import com.onlinestories.story_service.repository.ChapterVersionRepository;
import com.onlinestories.story_service.repository.StoryRepository;
import com.onlinestories.story_service.service.WritingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChapterPublishScheduler {

    private final ChapterRepository chapterRepository;
    private final StoryRepository storyRepository;
    private final ChapterVersionRepository chapterVersionRepository;
    private final WritingService writingService;


    @Scheduled(cron = "0 * * * * *") //
    public void scanAndPublishScheduledChapters() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        log.info("Scheduler Triggered: Checking for scheduled chapters to publish <= {}", now);

        // THÊM Query vào ChapterRepository: findByStatusAndPublishedAtLessThanEqual(ChapterStatus status, LocalDateTime time)
        List<Chapter> chaptersToPublish = chapterRepository.findByStatusAndPublishedAtLessThanEqual(ChapterStatus.SCHEDULED, now);

        if(chaptersToPublish.isEmpty()){
            return;
        }

        log.info("Found {} scheduled chapters to publish", chaptersToPublish.size());

        for (Chapter chapter : chaptersToPublish) {
            try {
                Story story = storyRepository.findById(chapter.getStoryId()).orElse(null);

                String targetVersionId = chapter.getPublishedVersionId();
                if (targetVersionId == null) {
                    log.warn("Chapter {} is SCHEDULED but has no PublishedVersionId. Cannot publish.", chapter.getChapterId());
                    continue;
                }

                ChapterVersion version = chapterVersionRepository.findById(targetVersionId).orElse(null);

                if (story != null && version != null) {
                    writingService.doPublishChapter(story, chapter, version);
                } else {
                    log.warn("Cannot auto-publish Chapter ID: {}. Associated Story or Version not found.", chapter.getChapterId());
                }
            } catch (Exception e) {
                log.error("Failed to auto-publish chapterId: {}. Reason: {}", chapter.getChapterId(), e.getMessage());
            }
        }
    }
}
