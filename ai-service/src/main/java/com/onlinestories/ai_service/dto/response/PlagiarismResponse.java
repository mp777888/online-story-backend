package com.onlinestories.ai_service.dto.response;

public record PlagiarismResponse(
        Boolean isPlagiarized,
        String message,
        String matchedStoryId,
        String matchedChapterId,
        String matchedContent
) {
}
