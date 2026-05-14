package com.example.onlinestories.transaction_service.repository;

import com.example.onlinestories.transaction_service.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface HistoryRepository extends MongoRepository<History, String> {
    Page<History> findByUserId(String userId, Pageable pageable);
    Page<History> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
