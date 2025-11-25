package com.onlinestories.user_service.Entity;


import com.onlinestories.user_service.Enum.ServicePackage;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Package {
    @Id
    String serviceId;
    ServicePackage servicePackage;
    Double price;
    Double discount;
    LocalDate startDate;
    LocalDate endDate;

    @DBRef
    List<User> listUsers;
}
