package com.example.onlinestories.transaction_service.Kafka.Producer;

import com.onlinestories.common.kafka.KafkaTopics;
import com.onlinestories.common.transaction.event.TransEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTransactionEvent(TransEvent event) {
        log.info("Sending Kafka Event USER_CREATED: userId={}", event.getUserId());
        if (event.getEventId() == null) event.setEventId(UUID.randomUUID().toString());
        if (event.getOccurredAt() == null) event.setOccurredAt(Instant.now());
        event.setEventType(event.getEventType());
        kafkaTemplate.send(KafkaTopics.TRANSACTION_EVENT, event.getUserId(), event);
    }
}
