package com.onlinestories.report_service.entity;

import com.onlinestories.common.report.enums.ReportReason;
import com.onlinestories.common.report.enums.ReportStatus;
import com.onlinestories.common.report.enums.ReportType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "reporter_reported_idx", def = "{'reporterId': 1, 'reportedId': 1}", unique = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Report {
    @Id
    String reportId;
    String reporterId;
    String reportedId;
    ReportReason reason;
    ReportType type;
    ReportStatus status;
    String content;
    LocalDateTime createdAt;
}
