//package com.onlinestories.report_service.scheduler;
//
//import com.onlinestories.common.report.enums.ReportType;
//import com.onlinestories.report_service.client.AIClient;
//import com.onlinestories.report_service.client.StoryClient;
//import com.onlinestories.report_service.entity.Report;
//import com.onlinestories.report_service.repository.ReportRepository;
//import com.onlinestories.report_service.service.ReportService;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
//public class ReportScheduler {
//    ReportRepository reportRepository;
//    ReportService reportService;
//    StoryClient storyClient;
//    AIClient aiClient;
//
//    @Scheduled(cron = "0 0/10 * * * ?") // Chạy 10 phút một lần
//    public void autoProcessPendingReports() {
//        List<Report> pendingReports = reportRepository.findByStatus("PENDING");
//
//        for (Report report : pendingReports) {
//            try {
//                ReportType type = report.getType();
//                switch (type){
//                    case STORY -> {
//
//                    }
//                    case COMMENT -> {
//
//                    }
//                    case CHAPTER -> {
//
//                    }
//                }
//
//            } catch (Exception e) {
//                log.error("Lỗi khi AI tự động xử lý report: {}", report.getReportId(), e);
//            }
//        }
//    }
//}
