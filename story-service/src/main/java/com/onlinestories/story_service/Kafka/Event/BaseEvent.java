package com.onlinestories.story_service.Kafka.Event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseEvent {
    @Builder.Default
    String eventId = UUID.randomUUID().toString();
    String eventType;
    @Builder.Default
    Instant occurredAt = Instant.now();
}
