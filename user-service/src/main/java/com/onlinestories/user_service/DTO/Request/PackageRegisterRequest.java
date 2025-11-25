package com.onlinestories.user_service.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PackageRegisterRequest {
    String userId;
    String servicePackage;
    Integer months;
}
