package com.onlinestories.common.story.event;


import com.onlinestories.common.kafka.BaseEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoryUpdatedEvent extends BaseEvent {
    String storyId;
    String title;
    String description;
    String authorId;
    String authorName;
    List<String> genres;
    List<String> tags;
    String coverImg;
    String status;
    int numberOfChapters;
    int numberOfViews;
    double averageRatingScore;
    int totalRatingCount;
    boolean premium;
    int unlockPrice;
}
