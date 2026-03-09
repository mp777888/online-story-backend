package com.onlinestories.user_service.Controller;

import com.onlinestories.user_service.DTO.Request.UserCreateRequest;
import com.onlinestories.user_service.DTO.Request.UserUpdateRequest;
import com.onlinestories.user_service.DTO.Response.UserResponse;
import com.onlinestories.user_service.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserCreateRequest request) {
        log.info("Received registration request for username={}", request.getUsername());
        return userService.createUser(request);
    }

    @GetMapping
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get profile for userId={}", userId);
        return userService.getMyInfo(userId);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("data") UserUpdateRequest request) {
        String userId = jwt.getSubject();
        log.info("Received profile update request for userId={}", userId);
        return userService.updateProfile(userId,file,request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        log.info("Received request to get user profile for userId={}", id);
        return userService.getUserById(id);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to delete account for userId={}", userId);
        return userService.deleteUser(userId);
    }

    @PostMapping("/follow")
    public ResponseEntity<String> followUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String followUserId
    ) {
        String userId = jwt.getSubject();
        log.info("Received follow request from userId={} to targetUserId={}",userId, followUserId);
        return userService.followUser(userId, followUserId);
    }

    @PostMapping("/unfollow")
    public ResponseEntity<String> unfollowUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String followUserId
    ) {
        String userId = jwt.getSubject();
        log.info("Received unfollow request from userId={} to targetUserId={}",userId, followUserId);
        return userService.unfollowUser(userId, followUserId);
    }

    @GetMapping("/follower")
    public ResponseEntity<List<UserResponse>> getFollowers(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get followers for userId={}", userId);
        return userService.getFollowers(userId);
    }

    @GetMapping("/following")
    public ResponseEntity<List<UserResponse>> getFollowing(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get following for userId={}", userId);
        return userService.getFollowing(userId);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("user-service alive");
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkUserExistence(@RequestParam String userId) {
        log.info("Received request to check existence for userId={}", userId);
        return userService.checkUserExistence(userId);
    }

}
