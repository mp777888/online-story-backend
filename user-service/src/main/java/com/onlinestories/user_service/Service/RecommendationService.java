package com.onlinestories.user_service.Service;

import com.onlinestories.user_service.Entity.User;
import com.onlinestories.user_service.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecommendationService {
    final MongoTemplate mongoTemplate;
    final UserRepository userRepository;

    public Boolean isCategoriesEmpty(String userId) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        return user.getCategories() == null || user.getCategories().isEmpty();
    }

    public ResponseEntity<String> addCategories(String userId, List<String> categories) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        user.getCategories().addAll(categories);
        userRepository.save(user);
        return ResponseEntity.ok("Categories added successfully");
    }

    public ResponseEntity<List<String>> getUserCategories(String userId) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user.getCategories().stream().toList());
    }

}
