package com.onlinestories.user_service.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PackageAddingRequest {
    String userId;
    String packageId;
    String email;
}
