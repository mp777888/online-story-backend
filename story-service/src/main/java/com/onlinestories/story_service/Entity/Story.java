package com.onlinestories.story_service.Entity;

import com.onlinestories.story_service.Enum.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
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
    Status status;
    String img;
    Integer audience; // 0: Young Adult, 1: New Adult, 2: Adult
    Integer numberOfChapters;
    Boolean isPublished;
    Set<GenreSummary> genres;
    List<Chapter> chapters;



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreSummary {
        private String genreId;
        private String name;
    }
}
