package com.onlinestories.story_service.Entity;

import com.onlinestories.story_service.Enum.ChapterStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
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
    String storyId;
    String title;
    String summary;
    String img;
    Integer chapterNumber;
    LocalDateTime createdAt;
    LocalDateTime publishedAt;
    ChapterStatus status;
    String audioUrl;

}
