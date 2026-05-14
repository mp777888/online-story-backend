package com.onlinestories.story_service.entity;

import com.onlinestories.story_service.enums.Language;
import com.onlinestories.story_service.enums.StoryStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Story {
    @Id
    String storyId;
    String authorId;
    String title;
    String description;
    StoryStatus status;
    Language language;
    String img;
    int numberOfViews;
    int numberOfChapters;
    @Indexed(direction = IndexDirection.DESCENDING)
    double averageRatingScore;
    double totalRatingScore;
    int totalRatingCount;
    Set<GenreSummary> genres;
    Set<String> tags;

    boolean premium = false;
    int unlockPrice = 0;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreSummary {
        private String genreId;
        private String name;
    }
    LocalDateTime createdAt;
}
