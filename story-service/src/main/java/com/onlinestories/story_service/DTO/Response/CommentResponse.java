package com.onlinestories.story_service.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String commentId;
    String chapterId;
    String userId;
    String content;
    String parentCommentId;
    LocalDateTime createdAt;

    List<CommentResponse> replies;
}
