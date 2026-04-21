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
public class ChapterSyncEvent extends BaseEvent {
    String chapterId;
    String storyId;
    String chapterTitle;
    String storyTitle;
    String title;
    String status;
    String content;
}
