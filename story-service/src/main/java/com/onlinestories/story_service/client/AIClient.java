package com.onlinestories.story_service.client;

import com.onlinestories.common.story.dto.PlagiarismRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ai-service")
public interface AIClient {

    @PostMapping("/api/ai/check-plagiarism")
    Boolean checkPlagiarism(@RequestBody PlagiarismRequest request
    );

}
