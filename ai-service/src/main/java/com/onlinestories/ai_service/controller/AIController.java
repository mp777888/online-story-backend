package com.onlinestories.ai_service.controller;

import com.onlinestories.ai_service.service.ChatService;
import com.onlinestories.ai_service.service.ImageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/ai")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AIController {
    ImageService imageService;
    ChatService chatService;

    @GetMapping("/generate-image")
    public String generateImage(@RequestParam String prompt) {
        log.info("Received request to generate image with prompt: {}", prompt);
        return imageService.generateImageBase64(prompt);
    }

    @GetMapping("/chat")
    public String chatWithAI(@RequestParam String message) {
        log.info("Received chat message from user: {}", message);
        return chatService.chatWithUser(message);
    }

}
