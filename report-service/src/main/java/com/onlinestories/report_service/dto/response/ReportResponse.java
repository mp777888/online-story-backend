package com.onlinestories.report_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportResponse {
    String reportId;
    String reporterId;
    String reportedId;
    String type;
    String reason;
    String status;
    String content;
    LocalDateTime createdAt;
}
