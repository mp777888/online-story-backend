package com.onlinestories.ai_service.controller;

import com.onlinestories.ai_service.dto.request.ContentSuggestionRequest;
import com.onlinestories.ai_service.dto.request.SpellingCorrectionRequest;
import com.onlinestories.ai_service.dto.response.PlagiarismResponse;
import com.onlinestories.ai_service.service.WritingAssistantService;
import com.onlinestories.ai_service.service.ChatService;
import com.onlinestories.ai_service.service.ImageService;
import com.onlinestories.ai_service.service.PlagiarismService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/ai")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AIController {
    ImageService imageService;
    ChatService chatService;
    PlagiarismService plagiarismService;
    WritingAssistantService writingAssistantService;

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

    @PostMapping("/check-plagiarism")
    public PlagiarismResponse checkPlagiarism(@RequestParam String chapterId) {
        log.info("Received plagiarism check request for chapterId: {}", chapterId);
        return plagiarismService.checkPlagiarism(chapterId);
    }

    @PostMapping("/correct-spelling")
    public String correctSpelling(@RequestBody SpellingCorrectionRequest request) {
        log.info("Received request to correct spelling");
        return writingAssistantService.correctSpelling(request.content());
    }

    @PostMapping("/suggest-content")
    public String suggestContent(@RequestBody ContentSuggestionRequest request) {
        log.info("Received request to suggest content");
        return writingAssistantService.suggestContent(request.currentContent(), request.promptDirection());
    }
}
