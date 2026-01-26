package com.onlinestories.story_service.Entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Chapter {
    @Id
    String chapterId;
    String storyId;
    String title;
    String summary;
    String img;
    Integer chapterNumber;
    LocalDateTime createdAt;
    String status;
    String audioUrl;

    List<ChapterVersion> versions;
    
}
