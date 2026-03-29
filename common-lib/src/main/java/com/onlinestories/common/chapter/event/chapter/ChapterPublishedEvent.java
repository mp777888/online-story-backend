package com.onlinestories.common.chapter.event.chapter;


import com.onlinestories.common.kafka.BaseEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

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
