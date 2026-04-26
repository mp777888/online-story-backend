package com.onlinestories.social_service.repository;

import com.onlinestories.social_service.model.Contest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestRepository extends MongoRepository<Contest, String> {
    List<Contest> findByIsActiveTrue();
}

