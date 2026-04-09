package com.example.onlinestories.transaction_service.Repostiory;

import com.example.onlinestories.transaction_service.Entity.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends MongoRepository<Wallet, String> {
    void deleteByUserId(String userId);
    Optional<Wallet> findByUserId(String userId);
}
