package com.onlinestories.user_service.controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.user_service.dto.response.NotificationResponse;

import com.onlinestories.user_service.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationController {
    NotificationService notificationService;

    @PostMapping("/read")
    public ApiResponse<String> markAsRead(String notificationId) {
        log.info("Received request to mark notification {} as read", notificationId);
        notificationService.markAsRead(notificationId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Notification marked as read successfully")
                .result(notificationId)
                .build();
    }

    @GetMapping("/my-notifications")
    public ApiResponse<Page<NotificationResponse>> getAllMyNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = jwt.getSubject();
        log.info("Received request to get notifications for userId={}, page={}, size={}", userId, page, size);
        return ApiResponse.<Page<NotificationResponse>>builder()
                .code(200)
                .message("Notifications retrieved successfully")
                .result(notificationService.getAllMyNotifications(userId, page, size))
                .build();
    }

    @DeleteMapping("/clear")
    public ApiResponse<String> clearAllMyNotifications(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to clear notifications for userId={}", userId);
        notificationService.clearAllMyNotifications(userId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("All notifications cleared successfully")
                .result("All notifications cleared successfully")
                .build();
    }

    @DeleteMapping("/delete")
    public ApiResponse<String> deleteNotification(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String notificationId) {
        String userId = jwt.getSubject();
        log.info("Received request to delete notification {} for userId={}", notificationId, userId);
        notificationService.deleteNotification(notificationId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Notification deleted successfully")
                .result("Notification deleted successfully")
                .build();
    }
}
