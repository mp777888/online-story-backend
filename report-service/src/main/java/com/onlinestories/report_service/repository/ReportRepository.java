package com.onlinestories.report_service.repository;

import com.onlinestories.report_service.entity.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, String> {
}
