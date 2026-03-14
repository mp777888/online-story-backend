package com.onlinestories.story_service.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DraftResponse {
    String draftId;
    String chapterId;
    String content;
    LocalDateTime lastSavedAt;
}
