package com.onlinestories.story_service.Entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@CompoundIndex(name = "chapter_date_idx", def = "{'chapterId': 1, 'date': 1}", unique = true)
public class ChapterDailyView {
    @Id
    String id;
    @Indexed
    String storyId;
    String chapterId;
    LocalDate date;
    int viewCount;
}
