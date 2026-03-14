package com.onlinestories.story_service.Entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChapterDraft {
    @Id
    String chapterDraftId;
    @Indexed(unique = true)
    String chapterId;
    String content;
    LocalDateTime lastSavedAt;
}
