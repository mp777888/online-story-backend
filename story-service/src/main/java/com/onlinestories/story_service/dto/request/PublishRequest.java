package com.onlinestories.story_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublishRequest {
    String storyId;
    String chapterId;
    String chapterVersionId;
    LocalDateTime publishDate;
}
