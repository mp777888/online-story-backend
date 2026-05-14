package com.onlinestories.user_service.repository;

import com.onlinestories.user_service.entity.User;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
