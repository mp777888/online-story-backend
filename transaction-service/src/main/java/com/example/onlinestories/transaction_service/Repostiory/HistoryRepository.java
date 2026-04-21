package com.example.onlinestories.transaction_service.Repostiory;

import com.example.onlinestories.transaction_service.Entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends MongoRepository<History, String> {
    Page<History> findByUserId(String userId, Pageable pageable);
}
