package com.onlinestories.report_service.kafka.producer;

import com.onlinestories.common.kafka.KafkaTopics;
import com.onlinestories.common.report.event.ReportResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReportEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendResponseEvent(ReportResponseEvent event) {
        log.info("Publishing ReportResponseEvent for reportId: {}, responderId: {}", event.getReportId(), event.getRespondedId());

        if (event.getEventId() == null) event.setEventId(UUID.randomUUID().toString());
        if (event.getOccurredAt() == null) event.setOccurredAt(Instant.now());
        event.setEventType(event.getEventType());

        kafkaTemplate.send(KafkaTopics.REPORT_RESPONDED, event.getReportId(), event);

        log.info("Successfully published ReportResponseEvent: {}", event.getEventId());
    }
}
