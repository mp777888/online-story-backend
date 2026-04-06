package com.onlinestories.story_service.Controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.story_service.Entity.Tag;
import com.onlinestories.story_service.Service.TagService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/stories/tags")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TagController {
    TagService tagService;

    @GetMapping("/suggest")
    public ApiResponse<List<String>> suggestTags(@RequestParam String keyword) {
        log.info("Received request to suggest tags for keyword: {}", keyword);
        return ApiResponse.<List<String>>builder()
                .code(200)
                .message("Tags suggested successfully")
                .result(tagService.suggestTags(keyword))
                .build();
    }

    @GetMapping("/trending")
    public ApiResponse<List<String>> getTrendingTags() {
        log.info("Received request to fetch trending tags");
        return ApiResponse.<List<String>>builder()
                .code(200)
                .message("Trending tags fetched successfully")
                .result(tagService.getTrendingTags())
                .build();
    }
}
