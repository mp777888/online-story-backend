package com.onlinestories.user_service.Controller;

import com.onlinestories.user_service.DTO.Request.SocialCreateRequest;
import com.onlinestories.user_service.DTO.Request.UserCreateRequest;
import com.onlinestories.user_service.DTO.Request.UserUpdateRequest;
import com.onlinestories.user_service.DTO.Response.UserResponse;
import com.onlinestories.user_service.Exception.ApiResponse;
import com.onlinestories.user_service.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

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
    public ApiResponse<UserResponse> registerUser(@RequestBody UserCreateRequest request) {
        log.info("Received registration request for username={}", request.getUsername());
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("User registered successfully")
                .result(userService.createUser(request))
                .build();
    }

    @PostMapping("/social")
    public ApiResponse<UserResponse> registerUserViaSocial(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SocialCreateRequest request) {
        String userId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        log.info("Received social registration request for userId={} with email={}", userId, email);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("User registered successfully via social login")
                .result(userService.createUserViaSocial(userId, email, request))
                .build();
    }

    @GetMapping
    public ApiResponse<UserResponse> getMyInfo(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get profile for userId={}", userId);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.getMyInfo(userId))
                .build();
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserResponse> updateUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("data") UserUpdateRequest request) {
        String userId = jwt.getSubject();
        log.info("Received profile update request for userId={}", userId);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("User profile updated successfully")
                .result(userService.updateProfile(userId, file, request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable String id) {
        log.info("Received request to get user profile for userId={}", id);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.getUserById(id))
                .build();
    }

    @DeleteMapping
    public ApiResponse<String> deleteUser(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to delete account for userId={}", userId);
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("User account deleted successfully")
                .build();
    }

    @PostMapping("/follow")
    public ApiResponse<String> followUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String followUserId
    ) {
        String userId = jwt.getSubject();
        log.info("Received follow request from userId={} to targetUserId={}",userId, followUserId);
        userService.followUser(userId, followUserId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Successfully followed user with id: " + followUserId)
                .build();
    }

    @PostMapping("/unfollow")
    public ApiResponse<String> unfollowUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String followUserId
    ) {
        String userId = jwt.getSubject();
        log.info("Received unfollow request from userId={} to targetUserId={}",userId, followUserId);
        userService.unfollowUser(userId, followUserId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Successfully unfollowed user with id: " + followUserId)
                .build();
    }

    @GetMapping("/follower")
    public ApiResponse<List<UserResponse>> getFollowers(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get followers for userId={}", userId);
        return ApiResponse.<List<UserResponse>>builder()
                .code(200)
                .result(userService.getFollowers(userId))
                .build();
    }

    @GetMapping("/following")
    public ApiResponse<List<UserResponse>> getFollowing(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get following for userId={}", userId);
        return ApiResponse.<List<UserResponse>>builder()
                .code(200)
                .result(userService.getFollowing(userId))
                .build();
    }

    @GetMapping("/is-following")
    public ApiResponse<Boolean> isFollowing(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String targetUserId
    ) {
        String userId = jwt.getSubject();
        log.info("Received request to check if userId={} is following targetUserId={}", userId, targetUserId);
        return ApiResponse.<Boolean>builder()
                .code(200)
                .result(userService.isFollowing(userId, targetUserId))
                .build();
    }

    @GetMapping("/exists")
    public Boolean checkUserExistence(@RequestParam String userId) {
        log.info("Received request to check existence for userId={}", userId);
        return userService.checkUserExistence(userId);
    }

}
