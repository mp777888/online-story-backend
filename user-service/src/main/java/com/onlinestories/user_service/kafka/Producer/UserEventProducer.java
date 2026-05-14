package com.onlinestories.user_service.kafka.Producer;

import com.onlinestories.common.kafka.KafkaTopics;
import com.onlinestories.common.user.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserCreatedEvent(UserEvent event) {
        log.info("Sending Kafka Event USER_CREATED: userId={}", event.getUserId());
        if (event.getEventId() == null) event.setEventId(UUID.randomUUID().toString());
        if (event.getOccurredAt() == null) event.setOccurredAt(Instant.now());
        event.setEventType(event.getEventType());
        kafkaTemplate.send(KafkaTopics.USER_CREATED, event.getUserId(), event);
        log.info("Successfully sent USER_CREATED event: {}", event.getEventId());
    }

    public void sendUserUpdatedEvent(UserEvent event) {
        log.info("Sending Kafka Event USER_UPDATED: userId={}", event.getUserId());
        if (event.getEventId() == null) event.setEventId(UUID.randomUUID().toString());
        if (event.getOccurredAt() == null) event.setOccurredAt(Instant.now());
        event.setEventType(event.getEventType());
        kafkaTemplate.send(KafkaTopics.USER_UPDATED, event.getUserId(), event);
        log.info("Successfully sent USER_UPDATED event: {}", event.getEventId());
    }
}
