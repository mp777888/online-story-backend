package com.onlinestories.report_service.service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.common.report.enums.ReportReason;
import com.onlinestories.common.report.enums.ReportStatus;
import com.onlinestories.common.report.enums.ReportType;
import com.onlinestories.report_service.client.StoryClient;
import com.onlinestories.report_service.client.UserClient;
import com.onlinestories.report_service.dto.request.ReportRequest;
import com.onlinestories.report_service.dto.response.ReportResponse;
import com.onlinestories.report_service.entity.Report;
import com.onlinestories.report_service.repository.ReportRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportService {
    ReportRepository reportRepository;
    UserClient userClient;
    StoryClient storyClient;

    public ReportResponse createReport(String userId, ReportRequest request){
        log.info("Creating report: {}", request);

        if(!userClient.checkUserExistence(userId)){
            log.error("Reporter user does not exist: {}", userId);
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        if(request.getType().equalsIgnoreCase("USER")){
            return userReport(userId, request);
        } else if(request.getType().equalsIgnoreCase("STORY")){
            return storyReport(userId, request);
        } else if(request.getType().equalsIgnoreCase("CHAPTER")){
            return chapterReport(userId, request);
        } else if(request.getType().equalsIgnoreCase("COMMENT")){
            return commentReport(userId, request);
        } else {
            log.error("Invalid report type: {}", request.getType());
            throw new IllegalArgumentException("Invalid report type");
        }
    }

    private ReportResponse userReport(String userId, ReportRequest request){
        log.info("Processing user report: {}", request);
        if(!userClient.checkUserExistence(request.getReportedId())){
            log.error("Reported user does not exist: {}", request.getReportedId());
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        Report report = Report.builder()
                .reporterId(userId)
                .reportedId(request.getReportedId())
                .type(ReportType.USER)
                .reason(ReportReason.valueOf(request.getReason()))
                .status(ReportStatus.PENDING)
                .content(request.getContent())
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
        reportRepository.save(report);
        log.info("Report saved: {}", report);

        return ReportResponse.builder()
                .reportId(report.getReportId())
                .reporterId(report.getReporterId())
                .reportedId(report.getReportedId())
                .type(report.getType().name())
                .reason(report.getReason().name())
                .status(report.getStatus().name())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private ReportResponse storyReport(String userId, ReportRequest request){
        log.info("Processing story report: {}", request);
        if(!storyClient.checkStoryExistence(request.getReportedId())){
            log.error("Reported story does not exist: {}", request.getReportedId());
            throw new AppException(ErrorCode.STORY_NOT_FOUND);
        }

        Report report = Report.builder()
                .reporterId(userId)
                .reportedId(request.getReportedId())
                .type(ReportType.STORY)
                .reason(ReportReason.valueOf(request.getReason()))
                .status(ReportStatus.PENDING)
                .content(request.getContent())
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
        reportRepository.save(report);
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .reporterId(report.getReporterId())
                .reportedId(report.getReportedId())
                .type(report.getType().name())
                .reason(report.getReason().name())
                .status(report.getStatus().name())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private ReportResponse chapterReport(String userId, ReportRequest request){
        log.info("Processing chapter report: {}", request);
        if(!storyClient.checkChapterExistence(request.getReportedId())){
            log.error("Reported chapter does not exist: {}", request.getReportedId());
            throw new AppException(ErrorCode.CHAPTER_NOT_FOUND);
        }

        Report report = Report.builder()
                .reporterId(userId)
                .reportedId(request.getReportedId())
                .type(ReportType.CHAPTER)
                .reason(ReportReason.valueOf(request.getReason()))
                .status(ReportStatus.PENDING)
                .content(request.getContent())
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
        reportRepository.save(report);
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .reporterId(report.getReporterId())
                .reportedId(report.getReportedId())
                .type(report.getType().name())
                .reason(report.getReason().name())
                .status(report.getStatus().name())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private ReportResponse commentReport(String userId, ReportRequest request){
        log.info("Processing comment report: {}", request);
        if(!storyClient.checkCommentExistence(request.getReportedId())){
            log.error("Reported comment does not exist: {}", request.getReportedId());
            throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
        }

        Report report = Report.builder()
                .reporterId(userId)
                .reportedId(request.getReportedId())
                .type(ReportType.COMMENT)
                .reason(ReportReason.valueOf(request.getReason()))
                .status(ReportStatus.PENDING)
                .content(request.getContent())
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
        reportRepository.save(report);
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .reporterId(report.getReporterId())
                .reportedId(report.getReportedId())
                .type(report.getType().name())
                .reason(report.getReason().name())
                .status(report.getStatus().name())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .build();
    }

    public ReportResponse getReportById(String reportId){
        log.info("Getting report by ID: {}", reportId);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    log.error("Report not found: {}", reportId);
                    return new AppException(ErrorCode.REPORT_NOT_FOUND);
                });
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .reporterId(report.getReporterId())
                .reportedId(report.getReportedId())
                .type(report.getType().name())
                .reason(report.getReason().name())
                .status(report.getStatus().name())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .build();
    }

    public Page<ReportResponse> getAllReports(int page, int size){
        log.info("Getting all reports - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reportRepository.findAll(pageable)
                .map(report -> ReportResponse.builder()
                        .reportId(report.getReportId())
                        .reporterId(report.getReporterId())
                        .reportedId(report.getReportedId())
                        .type(report.getType().name())
                        .reason(report.getReason().name())
                        .status(report.getStatus().name())
                        .content(report.getContent())
                        .createdAt(report.getCreatedAt())
                        .build());
    }
}
