package com.onlinestories.story_service.Entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@CompoundIndex(name = "chapter_parent_time_idx", def = "{'chapterId': 1, 'parentCommentId': 1, 'createdAt': -1}")
public class Comment {
    @Id
    String commentId;
    String chapterId;
    String userId;
    String content;
    @Indexed
    String parentCommentId;
    LocalDateTime createdAt;
}
