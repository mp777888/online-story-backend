package com.onlinestories.user_service.Service;

import com.onlinestories.user_service.DTO.Response.NotificationResponse;
import com.onlinestories.user_service.Entity.Notification;
import com.onlinestories.user_service.Repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    NotificationRepository notificationRepository;
    MongoTemplate mongoTemplate;

    public void sendNotification(String userId, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.info("Notification sent to user {}: {}", userId, message);
    }

    public void markAsRead(String notificationId) {
        Update update = new Update().set("isRead", true);
        Query query = new Query().addCriteria(Criteria.where("notificationId").is(notificationId));
        mongoTemplate.updateFirst(query, update, Notification.class);
        log.info("Notification {} marked as read", notificationId);
    }

    public Page<NotificationResponse> getAllMyNotifications(
            String userId, int page, int size) {
        log.info("Fetching notifications for user {}", userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByUserId(userId, pageable)
                .map(notification -> NotificationResponse.builder()
                        .notificationId(notification.getNotificationId())
                        .userId(notification.getUserId())
                        .message(notification.getMessage())
                        .isRead(notification.isRead())
                        .createdAt(notification.getCreatedAt())
                        .build());
    }


}
