package com.onlinestories.user_service.service;

import com.onlinestories.common.user.enums.NotiType;
import com.onlinestories.user_service.dto.response.NotificationResponse;
import com.onlinestories.user_service.entity.Notification;
import com.onlinestories.user_service.entity.User;
import com.onlinestories.user_service.repository.NotificationRepository;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    StringRedisTemplate stringRedisTemplate;
    NotificationRepository notificationRepository;
    MongoTemplate mongoTemplate;

    public void processFollowNotification(User follower, String targetUserId) {
        String redisKey = "cooldown:follow:notify:" + follower.getUserId() + ":" + targetUserId;

        // Kiểm tra xem Redis có key này chưa
        Boolean isOnCooldown = stringRedisTemplate.hasKey(redisKey);

        if (Boolean.FALSE.equals(isOnCooldown)) {
            // 1. Tiến hành lưu DB và gửi Notification cho user
            Notification notification = Notification.builder()
                    .userId(targetUserId)
                    .message(follower.getNickname() + " đã bắt đầu theo dõi bạn")
                    .isRead(false)
                    .type(NotiType.USER_FOLLOWED)
                    .refId(follower.getUserId())
                    .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                    .build();
            try{
                notificationRepository.save(notification);
                log.info("Follow notification sent to user {}: {}", targetUserId, notification.getMessage());
                // 2. Set cờ hạn chế trong Redis với TTL 1 giờ
                stringRedisTemplate.opsForValue().set(redisKey, "1", 1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("Failed to save follow notification for user {}: {}", targetUserId, e.getMessage());
            }
        } else {
            log.info("Follow notification for user {} is on cooldown. Skipping notification.", targetUserId);
        }
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
                        .type(notification.getType().name())
                        .refId(notification.getRefId())
                        .isRead(notification.isRead())
                        .createdAt(notification.getCreatedAt())
                        .build());
    }

    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
        log.info("Notification {} deleted", notificationId);
    }

    public void clearAllMyNotifications(String userId) {
        notificationRepository.deleteByUserId(userId);
        log.info("All notifications for user {} cleared", userId);
    }


}
