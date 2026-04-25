package com.onlinestories.story_service.Controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.story_service.DTO.Request.AddGenreRequest;
import com.onlinestories.common.story.dto.GenreResponse;
import com.onlinestories.story_service.DTO.Response.StoryResponse;

import com.onlinestories.story_service.Service.GenreService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/stories/genres")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GenreController {
    GenreService genreService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<GenreResponse> addGenre(@RequestBody AddGenreRequest request){
        log.info("Received request to add new genre: {}", request.getName());
        return ApiResponse.<GenreResponse>builder()
                .code(200)
                .message("Genre added successfully")
                .result(genreService.addGenre(request))
                .build();
    }

    @GetMapping("/search-stories")
    public ApiResponse<Page<StoryResponse>> getStoriesByGenre(
            @RequestParam String genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get stories for genre: {}", genreId);
        return ApiResponse.<Page<StoryResponse>>builder()
                .code(200)
                .message("Stories fetched successfully")
                .result(genreService.searchByGenre(genreId, page, size))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<GenreResponse>> getAllGenres() {
        log.info("Received request to fetch all genres");
        return ApiResponse.<List<GenreResponse>>builder()
                .code(200)
                .message("Genres fetched successfully")
                .result(genreService.getAllGenres())
                .build();
    }
}
