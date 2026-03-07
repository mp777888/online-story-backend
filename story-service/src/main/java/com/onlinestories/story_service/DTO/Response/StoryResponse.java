package com.onlinestories.story_service.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoryResponse {
    String storyId;
    String authorId;
    String title;
    String description;
    String status;
    String img;
    Integer numberOfChapters;
    Boolean isPublished;
    Set<String> genres;
}
