package com.onlinestories.user_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckInStatusResponse {
    LocalDate date;
    String dayOfWeek;
    boolean isCheckedIn;
    boolean isPastOrToday;
    boolean isToday;
}
