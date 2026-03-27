package com.onlinestories.story_service.Kafka.Event.story;

import com.onlinestories.story_service.Kafka.Event.BaseEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoryCreatedEvent extends BaseEvent {
    String storyId;
    String authorId;
    String title;
    Set<String> genreNames;
}
