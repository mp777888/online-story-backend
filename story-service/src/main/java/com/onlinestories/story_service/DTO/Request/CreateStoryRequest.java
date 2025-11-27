package com.onlinestories.story_service.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateStoryRequest {
    String authorId;
    String title;
    String description;
    String img;
    Set<String> genreIds;
}
