package com.onlinestories.story_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VersionResponse {
    String versionId;
    String chapterId;
    String versionName;
    String content;
    LocalDateTime createdAt;
}
