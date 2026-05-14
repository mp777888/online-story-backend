package com.onlinestories.story_service.dto.response;

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
    String language;
    int numberOfChapters;
    int numberOfViews;
    double averageRatingScore;
    int totalRatingCount;
    Set<String> genres;
    Set<String> tags;
    boolean premium;
    int unlockPrice;
}
