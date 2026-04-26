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
@Document(collection = "contest_votes")
public class ContestVote {
    @Id
    private String id;
    private String contestId;
    private String entryId;
    private String userId;
    private LocalDateTime votedAt;
}

