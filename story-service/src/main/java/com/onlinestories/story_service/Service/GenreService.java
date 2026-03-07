package com.onlinestories.story_service.Service;

import com.onlinestories.story_service.DTO.Request.AddGenreRequest;
import com.onlinestories.story_service.DTO.Response.GenreResponse;
import com.onlinestories.story_service.DTO.Response.StoryResponse;
import com.onlinestories.story_service.Entity.Genre;
import com.onlinestories.story_service.Entity.Story;
import com.onlinestories.story_service.Repository.GenreRepository;
import com.onlinestories.story_service.Repository.StoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreService {
    final GenreRepository genreRepository;
    final StoryRepository storyRepository;

    public ResponseEntity<GenreResponse> addGenre(AddGenreRequest request) {
        try{
            log.info("Adding new genre: {}", request.getName());
            var genre = genreRepository.findByName(request.getName());
            if (genre != null) {
                log.warn("Genre {} already exists", request.getName());
                return ResponseEntity.badRequest().build();
            }
            var newGenre = Genre.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .icon(request.getIcon())
                    .build();
            genreRepository.save(newGenre);

            GenreResponse genreResponse = GenreResponse.builder()
                    .genreId(newGenre.getGenreId())
                    .name(newGenre.getName())
                    .description(newGenre.getDescription())
                    .icon(newGenre.getIcon())
                    .build();
            log.info("Genre {} added successfully", request.getName());
            return ResponseEntity.ok(genreResponse);
        }
        catch (Exception e) {
            log.error("Error adding genre: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        try {
            log.info("Fetching all genres");
            List<Genre> genres = genreRepository.findAll();

            List<GenreResponse> genreResponses = genres.stream()
                    .map(genre -> GenreResponse.builder()
                            .genreId(genre.getGenreId())
                            .name(genre.getName())
                            .description(genre.getDescription())
                            .icon(genre.getIcon())
                            .build())
                    .toList();
            log.info("Fetched {} genres", genreResponses.size());
            return ResponseEntity.ok(genreResponses);
        }
        catch (Exception e) {
            log.error("Error fetching genres: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<Page<StoryResponse>> searchByGenre(
            String genreId, int page, int size) {
        try {
            log.info("Searching stories by genre: {}", genreId);
            if(!genreRepository.existsById(genreId)) {
                log.warn("Genre with id {} not found", genreId);
                return ResponseEntity.notFound().build();
            }

            Pageable pageable = PageRequest.of(page, size);

            Page<StoryResponse> storyResponses = storyRepository.findByGenresGenreId(genreId, pageable)
                    .map(story -> StoryResponse.builder()
                            .storyId(story.getStoryId())
                            .authorId(story.getAuthorId())
                            .title(story.getTitle())
                            .description(story.getDescription())
                            .img(story.getImg())
                            .status(story.getStatus().name())
                            .genres(story.getGenres().stream()
                                    .map(Story.GenreSummary::getName)
                                    .collect(Collectors.toSet()))
                            .build());

            return ResponseEntity.ok(storyResponses);
        }
        catch (Exception e) {
            log.error("Error searching stories by genre: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }

    }
}
