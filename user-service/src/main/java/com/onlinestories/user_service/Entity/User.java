package com.onlinestories.user_service.Entity;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    String userId;
    @Indexed(unique = true)
    String nickname;
    String img;
    LocalDate dob;
    String walletId;
    String description;
    LocalDateTime createdAt;
    Set<String> categories = new HashSet<>();
    double trendingScore;
    long storyCount;
    List<String> notificationIds = List.of();
    Set<String> followingIds = new HashSet<>();
    Set<String> followerIds = new HashSet<>();
    Set<LocalDate> checkInDates = new HashSet<>();
    int checkInStreak;
}
