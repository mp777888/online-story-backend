package com.onlinestories.story_service.Entity;

import com.onlinestories.story_service.Enum.Language;
import com.onlinestories.story_service.Enum.StoryStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
}
