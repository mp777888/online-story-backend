package com.example.onlinestories.transaction_service.repository;

import com.example.onlinestories.transaction_service.entity.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends MongoRepository<Wallet, String> {
    void deleteByUserId(String userId);
    Optional<Wallet> findByUserId(String userId);
}
