package com.onlinestories.social_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContestResponse {
    String id;
    String title;
    String description;
    String imageUrl;
    String startDate;
    String endDate;
    int totalEntries;
    int totalVotes;
}
