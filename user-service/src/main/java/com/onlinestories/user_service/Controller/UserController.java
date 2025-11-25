package com.onlinestories.user_service.Controller;

import com.onlinestories.user_service.DTO.Request.PackageRegisterRequest;
import com.onlinestories.user_service.DTO.Request.UserCreateRequest;
import com.onlinestories.user_service.DTO.Response.PackageResponse;
import com.onlinestories.user_service.DTO.Response.UserResponse;
import com.onlinestories.user_service.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserResponse> getMyInfo() {
        log.info("Received request to get user profile");
        return userService.getMyInfo();
    }



    @PostMapping("/service-package")
    public ResponseEntity<PackageResponse> registerServicePackage(@RequestBody PackageRegisterRequest request) {
        log.info("Received service package registration request for userId={}", request.getUserId());
        return userService.registerService(request);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("user-service alive");
    }
}
