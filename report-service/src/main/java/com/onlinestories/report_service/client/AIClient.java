package com.onlinestories.report_service.client;

import com.onlinestories.common.chapter.dto.ChapterDTOResponse;
import com.onlinestories.common.story.dto.StoryDTOResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "ai-service")
public interface AIClient {

    @GetMapping("/api/ai/content-violation")
    Boolean checkContentViolation(@RequestParam String content);

}
