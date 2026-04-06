package com.onlinestories.story_service.Entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tag {
    @Id
    String tagId;
    @Indexed(unique = true)
    String name;
    String displayName;
    long usageCount;
}
