package com.onlinestories.authentication_service.Controller;

import com.onlinestories.authentication_service.DTO.AuthRequest;
import com.onlinestories.authentication_service.Service.AuthService;
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
    public ResponseEntity<Map<String, Object>> authentication(@RequestBody AuthRequest request){
        log.info("Received authentication request for username={}", request.getUsername());
        return authService.authenticate(request);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> healthCheck(){
        log.info("Authentication service health check endpoint called");
        return ResponseEntity.ok("Authentication Service is up and running!");
    }


}
