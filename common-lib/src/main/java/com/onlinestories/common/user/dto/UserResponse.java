package com.onlinestories.common.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String userId;
    String username;
    String nickname;
    String gender;
    String email;
    String img;
    String description;
    LocalDateTime createdAt;
    LocalDate dob;
}
