package com.onlinestories.report_service.controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.report_service.dto.request.ReportRequest;
import com.onlinestories.report_service.dto.response.ReportResponse;
import com.onlinestories.report_service.service.ReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<ReportResponse>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        log.info("Retrieving reports - page: {}, size: {}", page, size);
        return ApiResponse.<Page<ReportResponse>>builder()
            .code(200)
            .message("Reports retrieved successfully")
            .result(reportService.getAllReports(page, size))
            .build();
    }

    @GetMapping("/id")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReportResponse> getReportById(
            @RequestParam String reportId) {
        log.info("Retrieving report by id: {}", reportId);
        return ApiResponse.<ReportResponse>builder()
                .code(200)
                .message("Report retrieved successfully")
                .result(reportService.getReportById(reportId))
                .build();
    }
}
