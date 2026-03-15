package com.onlinestories.story_service.DTO.Request;

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
