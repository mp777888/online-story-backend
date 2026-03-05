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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public ResponseEntity<List<StoryResponse>> searchByGenre(String genre){
        try {
            log.info("Searching stories by genre: {}", genre);
            Genre genreEntity = genreRepository.findByName(genre);
            if (genreEntity == null) {
                log.warn("Genre {} not found", genre);
                return ResponseEntity.badRequest().build();
            }
            List<Story> stories = storyRepository.findByGenresContaining(genreEntity);
            log.info("Found {} stories for genre {}", stories.size(), genre);

            List<StoryResponse> storyResponses = stories.stream()
                    .map(story -> StoryResponse.builder()
                    .storyId(story.getStoryId())
                    .authorId(story.getAuthorId())
                    .title(story.getTitle())
                    .description(story.getDescription())
                    .status(story.getStatus().name())
                    .genres(story.getGenres().stream().map(Genre::getName).collect(java.util.stream.Collectors.toSet()))
                    .build()).toList();

            return ResponseEntity.ok(storyResponses);
        }
        catch (Exception e) {
            log.error("Error searching stories by genre: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }

    }
}
