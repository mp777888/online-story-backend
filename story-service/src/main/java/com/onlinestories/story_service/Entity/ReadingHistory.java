package com.onlinestories.story_service.Entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "user_story_idx", def = "{'userId': 1, 'storyId': 1}", unique = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadingHistory {
    @Id
    String historyId;
    String userId;
    String storyId;
    String lastChapterId;
    Float percentageRead;
    LocalDateTime lastView;
    LocalDateTime lastReadAt;

    @Builder.Default
    Map<String, LocalDateTime> chapterViewTimes = new HashMap<>();
}
