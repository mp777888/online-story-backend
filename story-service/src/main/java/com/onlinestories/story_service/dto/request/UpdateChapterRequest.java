package com.onlinestories.story_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateChapterRequest {
    String storyId;
    String chapterId;
    String title;
    String status;
    LocalDateTime publishedAt;
}
