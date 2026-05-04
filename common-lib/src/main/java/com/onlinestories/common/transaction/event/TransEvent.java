package com.onlinestories.common.transaction.event;

import com.onlinestories.common.kafka.BaseEvent;
import com.onlinestories.common.transaction.enums.HistoryStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransEvent extends BaseEvent {
    String userId;
    int tokens;
    HistoryStatus status;
}
