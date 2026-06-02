package com.onlinestories.report_service.service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.common.report.enums.ReportReason;
import com.onlinestories.common.report.enums.ReportStatus;
import com.onlinestories.common.report.enums.ReportType;
import com.onlinestories.common.report.event.ReportResponseEvent;
import com.onlinestories.report_service.client.StoryClient;
import com.onlinestories.report_service.client.UserClient;
import com.onlinestories.report_service.dto.request.ReportRequest;
import com.onlinestories.report_service.dto.response.ReportResponse;
import com.onlinestories.report_service.entity.Report;
import com.onlinestories.report_service.kafka.producer.ReportEventProducer;
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
    ReportEventProducer reportEventProducer;

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
        if(storyClient.checkStoryExistence(request.getReportedId()) == null){
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
        if(storyClient.checkChapterExistence(request.getReportedId()) == null){
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

    public ReportResponse getReportById(String reportId, String requesterId, boolean isAdmin){
        log.info("Getting report by ID: {}", reportId);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    log.error("Report not found: {}", reportId);
                    return new AppException(ErrorCode.REPORT_NOT_FOUND);
                });
        boolean isOwner = report.getReporterId().equals(requesterId);
        if (!isAdmin && !isOwner) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

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

    public Page<ReportResponse> getMyReports(String userId, int page, int size){
        log.info("Getting reports for user: {}, page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reportRepository.findByReporterId(userId, pageable)
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

    public ReportResponse manuallyHandleReport(String reportId, ReportStatus status){
        log.info("Manually handling report - ID: {}, new status: {}", reportId, status);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    log.error("Report not found for manual handling: {}", reportId);
                    return new AppException(ErrorCode.REPORT_NOT_FOUND);
                });
        report.setStatus(status);
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

    public ReportResponse sendResponseToReporter(String reportId){
        log.info("Sending response to reporter - Report ID: {}, reportId", reportId);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    log.error("Report not found for sending response: {}", reportId);
                    return new AppException(ErrorCode.REPORT_NOT_FOUND);
                });

        if(report.getStatus() == ReportStatus.PENDING){
            log.error("Report has not been handled yet: {}", reportId);
            throw new AppException(ErrorCode.REPORT_HAS_NOT_BEEN_HANDLED);
        }

        ReportResponseEvent responseEvent = ReportResponseEvent.builder()
                .reportId(report.getReportId())
                .respondedId(report.getReporterId())
                .responseMessage(responseMessage(report.getType(), report.getStatus()))
                .build();
        reportEventProducer.sendResponseEvent(responseEvent);

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

    private String responseMessage(ReportType type, ReportStatus status){
        String vnType = mapToVnType(type);
        String decision = switch (status) {
            case RESOLVED -> "đã được giải quyết và hành động phù hợp đã được thực hiện.";
            case REJECTED -> "đã được xem xét nhưng không thấy vi phạm chính sách, do đó không có hành động nào được thực hiện.";
            default -> "đang được xem xét bởi đội ngũ quản trị viên.";
        };
        return """ 
                Xin chào,
               
                Báo cáo của bạn về %s %s
                
                Cảm ơn bạn đã giúp chúng tôi duy trì một cộng đồng lành mạnh và an toàn.
                
                Trân trọng,
                Đội ngũ quản trị viên
                """.formatted(vnType, decision);
    }

    private String mapToVnType(ReportType type){
        return switch (type) {
            case USER -> "người dùng";
            case STORY -> "truyện";
            case CHAPTER -> "chương";
            case COMMENT -> "bình luận";
        };
    }
}
