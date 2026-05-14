package com.onlinestories.story_service.entity;

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
@CompoundIndex(name = "user_story_idx", def = "{'userId': 1, 'storyId': 1}", unique = true)
public class Rating {
    @Id
    String voteId;
    String userId;
    String storyId;
    double ratingScore;
    String comment;
    @Indexed
    LocalDateTime ratedAt;
}
