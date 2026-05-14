package com.onlinestories.story_service.dto.response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayoutDTO {
    // Khi gọi Aggregation.group("storyDetails.authorId"), kết quả MongoDB trả ra field _id
    // Field id này sẽ được Spring Data map tự động với giá trị _id (chính là authorId)
    String id;
    int totalViews;
}