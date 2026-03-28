package com.onlinestories.user_service.Kafka.Consumer;

import com.onlinestories.common.chapter.event.ChapterPublishedEvent;
import com.onlinestories.user_service.Entity.Notification;

import com.onlinestories.common.kafka.KafkaTopics;
import com.onlinestories.user_service.Repository.NotificationRepository;
import com.onlinestories.user_service.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChapterEventConsumer {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = KafkaTopics.CHAPTER_PUBLISHED, groupId = "${spring.application.name}")
    public void listenChapterPublished(ChapterPublishedEvent event) {
        log.info("Received Kafka Event CHAPTER_PUBLISHED: storyId={}, chapterId={}",
                event.getStoryId(), event.getChapterId());
        try {
            userRepository.findById(event.getAuthorId()).ifPresent(author -> {

                Set<String> followerIds = author.getFollowerIds();

                if (followerIds != null && !followerIds.isEmpty()) {
                    // Tạo danh sách Notification
                    List<Notification> notifications = followerIds.stream().map(followerId ->
                            Notification.builder()
                                    .userId(followerId)
                                    // Tạo message hiển thị cho user
                                    .message("Tác giả " + author.getNickname() + " vừa đăng chương mới: " + event.getTitle())
                                    .isRead(false)
                                    .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                                    .build()
                    ).toList();
                    notificationRepository.saveAll(notifications);

                    // websocket
                    for (Notification notification : notifications) {
                        messagingTemplate.convertAndSend("/topic/user/" + notification.getUserId(), notification);
                    }

                    log.info("Successfully saved {} notifications for followers of author: {}",
                            notifications.size(), author.getNickname());
                } else {
                    log.info("Author {} has no followers. No notifications sent.", author.getNickname());
                }
            });
        } catch (Exception e) {
            log.error("Error processing ChapterPublishedEvent: {}", e.getMessage(), e);
        }
    }
}
