package com.onlinestories.report_service.controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.common.report.enums.ReportStatus;
import com.onlinestories.common.utils.JwtUtils;
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

import java.util.List;

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
        String userId = JwtUtils.getSubject(jwt);
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

    @GetMapping("/my")
    public ApiResponse<Page<ReportResponse>> getMyReports(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        String userId = JwtUtils.getSubject(jwt);
        log.info("Retrieving reports for user: {} - page: {}, size: {}", userId, page, size);
        return ApiResponse.<Page<ReportResponse>>builder()
            .code(200)
            .message("User's reports retrieved successfully")
            .result(reportService.getMyReports(userId, page, size))
            .build();
    }

    @GetMapping("/id")
    public ApiResponse<ReportResponse> getReportById(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String reportId) {
        log.info("Retrieving report by id: {}", reportId);
        String requesterId = jwt.getSubject();
        boolean isAdmin = jwt.getClaimAsMap("realm_access") != null
                && ((List<String>) jwt.getClaimAsMap("realm_access").getOrDefault("roles", List.of()))
                .contains("ADMIN");

        return ApiResponse.<ReportResponse>builder()
                .code(200)
                .message("Report retrieved successfully")
                .result(reportService.getReportById(reportId, requesterId, isAdmin))
                .build();
    }

    @PutMapping("/manual-handle")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReportResponse> handleReportManually(
            @RequestParam String reportId,
            @RequestParam ReportStatus status
            ){
        log.info("Manually handling report - reportId: {}, newStatus: {}", reportId, status);
        return ApiResponse.<ReportResponse>builder()
                .code(200)
                .message("Report handled successfully")
                .result(reportService.manuallyHandleReport(reportId, status))
                .build();
    }

    @PostMapping("/response")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReportResponse> respondToReport(@RequestParam String reportId){
        log.info("Responding to report - reportId: {}", reportId);
        return ApiResponse.<ReportResponse>builder()
                .code(200)
                .message("Report response sent successfully")
                .result(reportService.sendResponseToReporter(reportId))
                .build();
    }
}
