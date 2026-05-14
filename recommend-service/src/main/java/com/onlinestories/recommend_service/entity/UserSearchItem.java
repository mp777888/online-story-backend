package com.onlinestories.recommend_service.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSearchItem {
    String userId;
    String nickname;
    String description;
    String img;
    float[] embedding;
}
