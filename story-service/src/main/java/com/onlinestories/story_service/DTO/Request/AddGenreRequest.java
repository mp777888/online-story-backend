package com.onlinestories.story_service.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddGenreRequest {
    String name;
    String icon;
    String description;
}
