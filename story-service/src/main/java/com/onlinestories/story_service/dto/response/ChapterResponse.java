package com.onlinestories.story_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChapterResponse {
    String chapterId;
    String storyId;
    String title;
    String summary;
    String status;
    String content;
    String img;
    String audioUrl;
    int numberOfViews;
    Float percentageRead;
    LocalDateTime createdAt;
    LocalDateTime publishedAt;
    LocalDateTime lastEditedAt;
}
