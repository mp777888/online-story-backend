package com.onlinestories.common.chapter.event;


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
public class ChapterTakenDownEvent extends BaseEvent {
    String chapterName;
    String storyId;
    String storyName;
    String authorId;
}
