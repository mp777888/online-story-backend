package com.onlinestories.common.user.event;


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
public class UserCreatedEvent extends BaseEvent {
    String userId;
    String nickname;
    String description;
    String img;
}
