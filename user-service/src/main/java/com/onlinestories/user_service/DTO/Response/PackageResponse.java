package com.onlinestories.user_service.DTO.Response;

import com.onlinestories.user_service.Entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PackageResponse {
    String serviceId;
    String servicePackage;
    List<User> listUsers;
    Double price;
    Double discount;
    LocalDate startDate;
    LocalDate endDate;
}
