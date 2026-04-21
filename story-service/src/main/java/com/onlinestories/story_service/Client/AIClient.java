package com.onlinestories.story_service.Client;

import com.onlinestories.common.story.dto.PlagiarismRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "ai-service")
public interface AIClient {

    @PostMapping("/api/ai/check-plagiarism")
    Boolean checkPlagiarism(@RequestBody PlagiarismRequest request
    );

}
