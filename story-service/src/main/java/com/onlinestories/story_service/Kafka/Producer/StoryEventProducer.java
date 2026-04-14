package com.onlinestories.story_service.Kafka.Producer;

import com.onlinestories.common.kafka.KafkaTopics;
import com.onlinestories.common.story.event.StoryMetricsSyncEvent;
import com.onlinestories.common.story.event.StoryUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class StoryEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void storyUpdatedEvent(StoryUpdatedEvent event) {
        log.info("Publishing StoryUpdatedEvent for storyId: {}", event.getStoryId());
        if (event.getEventId() == null) event.setEventId(UUID.randomUUID().toString());
        if (event.getOccurredAt() == null) event.setOccurredAt(Instant.now());
        event.setEventType(event.getEventType());

        kafkaTemplate.send(KafkaTopics.STORY_UPDATED, event.getStoryId(), event);
        log.info("Successfully published StoryUpdatedEvent: {}", event.getEventId());
    }

    public void storyMetricsUpdatedEvent(StoryMetricsSyncEvent event) {
        log.info("Publishing StoryMetricsUpdatedEvent for storyId: {}", event.getStoryId());
        if (event.getEventId() == null) event.setEventId(UUID.randomUUID().toString());
        if (event.getOccurredAt() == null) event.setOccurredAt(Instant.now());
        event.setEventType(event.getEventType());

        kafkaTemplate.send(KafkaTopics.STORY_METRICS_UPDATED, event.getStoryId(), event);
        log.info("Successfully published StoryMetricsUpdatedEvent: {}", event.getEventId());
    }
}
