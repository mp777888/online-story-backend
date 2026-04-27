package com.example.onlinestories.transaction_service.Repostiory;

import com.example.onlinestories.transaction_service.Entity.PendingPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PendingPaymentRepository extends MongoRepository<PendingPayment, String> {
    Optional<PendingPayment> findByTxnRef(String txnRef);
    Page<PendingPayment> findByCreatedAtBetween(LocalDateTime startOfMonth, LocalDateTime endOfMonth, Pageable pageable);
}
