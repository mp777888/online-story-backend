package com.onlinestories.user_service.kafka.Consumer;

import com.onlinestories.common.chapter.event.ChapterPublishedEvent;
import com.onlinestories.common.chapter.event.ChapterTakenDownEvent;
import com.onlinestories.common.report.event.ReportResponseEvent;
import com.onlinestories.common.transaction.enums.HistoryStatus;
import com.onlinestories.common.transaction.event.TransEvent;
import com.onlinestories.common.user.enums.NotiType;
import com.onlinestories.user_service.entity.Notification;

import com.onlinestories.common.kafka.KafkaTopics;
import com.onlinestories.user_service.repository.NotificationRepository;
import com.onlinestories.user_service.repository.UserRepository;
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
public class UserEventConsumer {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = KafkaTopics.CHAPTER_PUBLISHED, groupId = "${spring.application.name}")
    public void listenChapterPublished(ChapterPublishedEvent event) {
        log.info("Received Kafka Event CHAPTER_PUBLISHED: storyId={}, chapterId={}",
                event.getStoryId(), event.getChapterId());
        try {
            userRepository.findById(event.getAuthorId()).ifPresent(author -> {
                String message;
                if(event.getEventType().equals("CHAPTER_SCHEDULED")){
                    message = "Tác giả " + author.getNickname() + " vừa lên lịch đăng chương mới: " + event.getTitle();
                }
                else if(event.getEventType().equals("CHAPTER_PUBLISHED")){
                    message = "Tác giả " + author.getNickname() + " vừa đăng chương mới: " + event.getTitle();
                }
                else{
                    log.warn("Unknown event type: {}. No notifications will be sent.", event.getEventType());
                    return;
                }

                Set<String> followerIds = author.getFollowerIds();

                if (followerIds != null && !followerIds.isEmpty()) {
                    // Tạo danh sách Notification
                    List<Notification> notifications = followerIds.stream().map(followerId ->
                            Notification.builder()
                                    .userId(followerId)
                                    // Tạo message hiển thị cho user
                                    .message(message)
                                    .isRead(false)
                                    .type(NotiType.CHAPTER_PUBLISHED)
                                    .refId(event.getChapterId())
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

    @KafkaListener(topics = KafkaTopics.CHAPTER_PUBLISHED_APPROVED, groupId = "${spring.application.name}")
    public void listenChapterPublishedApproved(ChapterPublishedEvent event) {
        log.info("Received Kafka Event CHAPTER_PUBLISHED_APPROVED: storyId={}, chapterId={}",
                event.getStoryId(), event.getChapterId());
        try {
            userRepository.findById(event.getAuthorId()).ifPresent(author -> {
                String message = "Chương mới của bạn đã được duyệt và đăng lên: " + event.getTitle();

                Notification notification = Notification.builder()
                        .userId(event.getAuthorId())
                        .message(message)
                        .isRead(false)
                        .type(NotiType.CHAPTER_PUBLISHED)
                        .refId(event.getChapterId())
                        .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                        .build();
                notificationRepository.save(notification);

                // websocket
                messagingTemplate.convertAndSend("/topic/user/" + notification.getUserId(), notification);

                log.info("Successfully saved notification for chapter approval: chapterId={}, userId={}",
                        event.getChapterId(), event.getAuthorId());
            });
        } catch (Exception e) {
            log.error("Error processing ChapterPublishedApprovedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.REPORT_RESPONDED, groupId = "${spring.application.name}")
    public void listenReportResponded(ReportResponseEvent event){
        log.info("Received Kafka Event REPORT_RESPONDED: reportId={}, respondedId={}",
                event.getReportId(), event.getRespondedId());
        try {
            userRepository.findById(event.getRespondedId()).ifPresent(user ->
                    log.info("Found user for report response: userId={}, nickname={}",
                    user.getUserId(), user.getNickname()));

            String message = "Báo cáo của bạn đã được phản hồi: \n" + event.getResponseMessage();

            Notification notification = Notification.builder()
                    .userId(event.getRespondedId())
                    .message(message)
                    .isRead(false)
                    .type(NotiType.REPORT)
                    .refId(event.getReportId())
                    .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                    .build();
            notificationRepository.save(notification);

            // websocket
            messagingTemplate.convertAndSend("/topic/user/" + notification.getUserId(), notification);

            log.info("Successfully saved notification for report response: reportId={}, userId={}",
                    event.getReportId(), event.getRespondedId());
        } catch (Exception e) {
            log.error("Error processing ReportResponseEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.TRANSACTION_EVENT, groupId = "${spring.application.name}")
    public void listenTransactionEvent(TransEvent event){
        log.info("Received Kafka Event TRANSACTION_EVENT: userId={}, amount={}, eventType={}",
                event.getUserId(), event.getTokens(), event.getEventType());
        try {
            userRepository.findById(event.getUserId()).ifPresent(user ->
                    log.info("Found user for transaction event: userId={}, nickname={}",
                            user.getUserId(), user.getNickname()));

            String message;

            if(event.getStatus().equals(HistoryStatus.EARNED)){
                message = "Bạn vừa nhận được " + event.getTokens() + " xu từ người đọc mua truyện của bạn ";
            }
            else if(event.getStatus().equals(HistoryStatus.PAYOUT)){
                message = "Bạn vừa được nhận " + event.getTokens() + " xu từ việc viết truyện hàng tháng. Hãy kiểm tra ngay!";
            }
            else{
                log.warn("Unknown transaction status: {}. No notifications will be sent.", event.getStatus());
                return;
            }


            Notification notification = Notification.builder()
                    .userId(event.getUserId())
                    .message(message)
                    .isRead(false)
                    .type(NotiType.TRANSACTION)
                    .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                    .build();
            notificationRepository.save(notification);

            // websocket
            messagingTemplate.convertAndSend("/topic/user/" + notification.getUserId(), notification);

            log.info("Successfully saved notification for userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing TransactionEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.CHAPTER_TAKEN_DOWN, groupId = "${spring.application.name}")
    public void listenChapterTakenDownEvent(ChapterTakenDownEvent event) {
        log.info("Received Kafka Event CHAPTER_TAKEN_DOWN: storyId={}, chapter);Name={}",
                event.getStoryId(), event.getChapterName());
        try {
            userRepository.findById(event.getAuthorId()).ifPresent(author -> {
                String message = "Chương " + event.getChapterName() + " của bạn đã bị gỡ xuống vì vi phạm nội quy. Vui lòng kiểm tra lại nội dung chương và phản hồi cho chúng tôi biết nếu đây là sự nhầm lẫn. Xin cảm ơn!";


                Notification notification = Notification.builder()
                        .userId(event.getAuthorId())
                        .message(message)
                        .isRead(false)
                        .type(NotiType.REPORT)
                        .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                        .build();

                notificationRepository.save(notification);

                // websocket
                messagingTemplate.convertAndSend("/topic/user/" + notification.getUserId(), notification);
                log.info("Successfully saved notification for userId={}", event.getAuthorId());

            });
        } catch (Exception e) {
            log.error("Error processing ChapterTakenDownEvent: {}", e.getMessage(), e);
        }
    }
}
