package com.onlinestories.report_service.repository;

import com.onlinestories.report_service.entity.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReportRepository extends MongoRepository<Report, String> {
    List<Report> findByStatus(String pending);
}
