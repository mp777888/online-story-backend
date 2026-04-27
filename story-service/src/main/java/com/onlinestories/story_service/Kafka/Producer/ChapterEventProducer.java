package com.onlinestories.story_service.Kafka.Producer;

import com.onlinestories.common.chapter.event.ChapterPublishedEvent;
import com.onlinestories.common.chapter.event.ChapterSyncEvent;
import com.onlinestories.common.kafka.KafkaTopics;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChapterEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void publishChapterCreatedEvent(ChapterPublishedEvent event) {
        log.info("Publishing ChapterPublishedEvent for chapterId: {}, storyId: {}", event.getChapterId(), event.getStoryId());

        // Cấu hình một số metadata mặc định của BaseEvent nếu chưa có
        if (event.getEventId() == null) event.setEventId(UUID.randomUUID().toString());
        if (event.getOccurredAt() == null) event.setOccurredAt(Instant.now());
        event.setEventType(event.getEventType());

        // Gửi event vào topic CHAPTER_PUBLISHED, dùng storyId làm partition key
        // (để đảm bảo các event của cùng 1 truyện nhảy vào cùng 1 partition và được xử lý theo thứ tự)
        kafkaTemplate.send(KafkaTopics.CHAPTER_PUBLISHED, event.getStoryId(), event);

        log.info("Successfully published ChapterPublishedEvent: {}", event.getEventId());
    }

    public void publishChapterApprovedEvent(ChapterPublishedEvent event) {
        log.info("Publishing ChapterPublishedApprovedEvent for chapterId: {}, storyId: {}", event.getChapterId(), event.getStoryId());

        // Cấu hình một số metadata mặc định của BaseEvent nếu chưa có
        if (event.getEventId() == null) event.setEventId(UUID.randomUUID().toString());
        if (event.getOccurredAt() == null) event.setOccurredAt(Instant.now());
        event.setEventType(event.getEventType());

        kafkaTemplate.send(KafkaTopics.CHAPTER_PUBLISHED_APPROVED, event.getStoryId(), event);

        log.info("Successfully published ChapterPublishedApprovedEvent: {}", event.getEventId());
    }

    public void publishChapterSyncEvent(ChapterSyncEvent event) {
        log.info("Publishing ChapterSyncEvent for chapterId: {}, storyId: {}", event.getChapterId(), event.getStoryId());

        // Cấu hình một số metadata mặc định của BaseEvent nếu chưa có
        if (event.getEventId() == null) event.setEventId(UUID.randomUUID().toString());
        if (event.getOccurredAt() == null) event.setOccurredAt(Instant.now());
        event.setEventType(event.getEventType());

        kafkaTemplate.send(KafkaTopics.CHAPTER_SYNC, event.getStoryId(), event);

        log.info("Successfully published ChapterSyncEvent: {}", event.getEventId());
    }
}
