package com.onlinestories.user_service.Repository;

import com.onlinestories.user_service.Entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
