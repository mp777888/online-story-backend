package com.example.onlinestories.transaction_service.Repostiory;

import com.example.onlinestories.transaction_service.Entity.History;
import com.example.onlinestories.transaction_service.Entity.PayoutHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayoutHistoryRepository extends MongoRepository<PayoutHistory, String> {
    Page<PayoutHistory> findByPayoutMonth(String payMonth, Pageable pageable);
}
