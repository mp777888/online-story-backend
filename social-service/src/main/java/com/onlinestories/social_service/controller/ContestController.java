package com.onlinestories.social_service.controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.social_service.dto.request.ContestRequest;
import com.onlinestories.social_service.dto.response.ContestResponse;
import com.onlinestories.social_service.service.ContestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ContestController {
    ContestService contestService;

    @PostMapping("/contests/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ContestResponse> createContest(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("request") ContestRequest request) {
        log.info("Received request to create contest with title: {} from admin", request.getTitle());
        return ApiResponse.<ContestResponse>builder()
                .code(200)
                .message("Contest created successfully")
                .result(contestService.createContest(request, file))
                .build();
    }
}
