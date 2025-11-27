package com.onlinestories.story_service.Entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
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
    Integer chapterNumber;
    LocalDate createdAt;
    String status;
    byte[] audioData;

    List<ChapterVersion> versions;
    
}
