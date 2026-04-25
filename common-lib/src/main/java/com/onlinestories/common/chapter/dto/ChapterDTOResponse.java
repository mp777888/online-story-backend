package com.onlinestories.common.chapter.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChapterDTOResponse {
    String chapterId;
    String storyId;
    String authorId;
    String title;
    String content;
    LocalDateTime publishedDate;
}
