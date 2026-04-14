package com.onlinestories.story_service.Entity;

import com.onlinestories.story_service.Enum.ChapterStatus;
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
public class Chapter {
    @Id
    String chapterId;
    @Indexed
    String storyId;
    String title;
    String summary;
    String img;
    LocalDateTime createdAt;
    LocalDateTime publishedAt;
    ChapterStatus status;
    String publishedVersionId;
    String audioUrl;
    int numberOfViews;
}
