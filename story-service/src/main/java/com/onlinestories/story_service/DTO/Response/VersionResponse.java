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
public class VersionResponse {
    String versionId;
    String chapterId;
    String versionName;
    String content;
    LocalDateTime createdAt;
}
