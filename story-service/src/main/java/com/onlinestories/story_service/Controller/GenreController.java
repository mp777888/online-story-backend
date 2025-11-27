package com.onlinestories.story_service.Controller;

import com.onlinestories.story_service.DTO.Request.AddGenreRequest;
import com.onlinestories.story_service.DTO.Response.GenreResponse;
import com.onlinestories.story_service.Service.GenreService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/stories/genres")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GenreController {
    GenreService genreService;

    @PostMapping
    public ResponseEntity<GenreResponse> addGenre(@RequestBody AddGenreRequest request){
        log.info("Received request to add new genre: {}", request.getName());
        return genreService.addGenre(request);
    }
}
