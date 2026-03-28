package com.onlinestories.authentication_service.Controller;

import com.onlinestories.authentication_service.DTO.AuthRequest;

import com.onlinestories.authentication_service.Service.AuthService;
import com.onlinestories.common.exception.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthController {
    AuthService authService;

    @PostMapping
    public ApiResponse<Map<String, Object>> authentication(@RequestBody AuthRequest request){
        log.info("Received authentication request for username={}", request.getUsername());
        return ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Authentication successful")
                .result(authService.authenticate(request))
                .build();
    }

    @PostMapping("/social")
    public ApiResponse<Map<String, Object>> socialAuthentication(
            @RequestParam String code,
            @RequestParam String redirectUri
    ){
        log.info("Received Social authentication request with code={} and redirectUri={}", code, redirectUri);
        return ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Social authentication successful")
                .result(authService.authenticateBySocial(code, redirectUri))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refreshToken(@RequestParam String refreshToken){
        log.info("Received token refresh request");
        return ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Token refreshed successfully")
                .result(authService.refreshToken(refreshToken))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestParam String refreshToken){
        log.info("Received logout request with token={}", refreshToken);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Logout successful")
                .result(authService.logout(refreshToken))
                .build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Map<String,Object>> forgotPassword(@RequestParam String email){
        log.info("Received forgot password request for email={}", email);
        return ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Password reset email sent successfully")
                .result(authService.forgotPassword(email))
                .build();
    }

    @GetMapping("/ping")
    public ResponseEntity<String> healthCheck(){
        log.info("Authentication service health check endpoint called");
        return ResponseEntity.ok("Authentication Service is up and running!");
    }


}
