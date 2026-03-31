package com.onlinestories.report_service.controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.report_service.dto.request.ReportRequest;
import com.onlinestories.report_service.dto.response.ReportResponse;
import com.onlinestories.report_service.service.ReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReportController {
    ReportService reportService;

    @PostMapping
    public ApiResponse<ReportResponse> createReport(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody ReportRequest reportRequest
    ){
        String userId = jwt.getSubject();
        log.info("Received report request: {}", reportRequest);
        return ApiResponse.<ReportResponse>builder()
            .code(200)
            .message("Report created successfully")
            .result(reportService.createReport(userId, reportRequest))
            .build();
    }
}
