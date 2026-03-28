package com.onlinestories.common.chapter.event.interaction;


import com.onlinestories.common.kafka.BaseEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentAddedEvent extends BaseEvent {
   String commentId;
   String chapterId;
   String storyId;
   String userId;
   String parentCommentId;
}
