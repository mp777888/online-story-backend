package com.onlinestories.story_service.Kafka.Event.chapter;

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
public class ChapterPublishedEvent extends BaseEvent {
    String chapterId;
    String storyId;
    String title;
    String authorId;
}
