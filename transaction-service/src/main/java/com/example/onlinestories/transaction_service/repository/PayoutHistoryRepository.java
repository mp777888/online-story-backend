package com.example.onlinestories.transaction_service.repository;

import com.example.onlinestories.transaction_service.entity.PayoutHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayoutHistoryRepository extends MongoRepository<PayoutHistory, String> {
    Page<PayoutHistory> findByPayoutMonth(String payMonth, Pageable pageable);
}
