package com.onlinestories.story_service.Controller;

import com.onlinestories.story_service.Service.AzureTtsService;
import com.onlinestories.story_service.Service.ChapterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/chapters")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChapterController {
    ChapterService chapterService;

    @GetMapping
    public String getAudioUrl(@RequestParam String text, @RequestParam String language){
        log.info("Received request to get audio URL for text in language: {}", language);
        return chapterService.getAudioUrl(text,language); // Placeholder
    }

}
