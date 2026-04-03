package com.onlinestories.common.story.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoryDTOResponse {
    String storyId;
    String authorId;
    String status;
    String title;
    boolean premium;
    int unlockPrice;
}
