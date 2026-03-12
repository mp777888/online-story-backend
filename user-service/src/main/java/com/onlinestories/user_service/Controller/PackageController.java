package com.onlinestories.user_service.Controller;

import com.onlinestories.user_service.DTO.Request.PackageRegisterRequest;
import com.onlinestories.user_service.DTO.Request.UserCreateRequest;
import com.onlinestories.user_service.DTO.Request.UserUpdateRequest;
import com.onlinestories.user_service.DTO.Response.PackageResponse;
import com.onlinestories.user_service.DTO.Response.UserResponse;
import com.onlinestories.user_service.Service.PackageService;
import com.onlinestories.user_service.Service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/packages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PackageController {

    PackageService packageService;

    @PostMapping
    public ResponseEntity<PackageResponse> registerServicePackage(@RequestBody PackageRegisterRequest request) {
        log.info("Received service package registration request for userId={}", request.getUserId());
        return packageService.registerService(request);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("package-service alive");
    }
}
