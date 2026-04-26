package com.onlinestories.social_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "participation")
public class ContestParticipation {
    @Id
    private String id;
    private String contestId;
    private String userId;
    private boolean hasParticipated;
    private LocalDateTime participatedAt;
}

