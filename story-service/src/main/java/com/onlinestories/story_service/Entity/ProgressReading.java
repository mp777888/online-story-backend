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
@CompoundIndex(name = "user_chapter_idx", def = "{'userId': 1, 'chapterId': 1}", unique = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgressReading {
    @Id
    String progressId;
    String userId;
    String chapterId;
    Float percentageRead;
    LocalDateTime lastReadAt;
    LocalDateTime lastView;
}
