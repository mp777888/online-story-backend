package com.onlinestories.story_service.Kafka.Event.interaction;

import com.onlinestories.story_service.Kafka.Event.BaseEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingAddedEvent extends BaseEvent {
   String ratingId;
   String storyId;
   String userId;
   double ratingScore;
}
