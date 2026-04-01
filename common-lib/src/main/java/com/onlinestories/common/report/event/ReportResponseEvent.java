package com.onlinestories.common.report.event;


import com.onlinestories.common.kafka.BaseEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportResponseEvent extends BaseEvent {
    String reportId;
    String respondedId;
    String responseMessage;
}
