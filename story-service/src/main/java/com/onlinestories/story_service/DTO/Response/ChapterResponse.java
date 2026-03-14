package com.onlinestories.story_service.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

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
    String img;
    LocalDateTime createdAt;
}
