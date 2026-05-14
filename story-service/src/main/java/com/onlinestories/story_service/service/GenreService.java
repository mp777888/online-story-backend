package com.onlinestories.story_service.service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.story_service.dto.request.AddGenreRequest;
import com.onlinestories.common.story.dto.GenreResponse;
import com.onlinestories.story_service.dto.response.StoryResponse;
import com.onlinestories.story_service.entity.Genre;
import com.onlinestories.story_service.entity.Story;

import com.onlinestories.story_service.repository.GenreRepository;
import com.onlinestories.story_service.repository.StoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public GenreResponse addGenre(AddGenreRequest request) {
        try{
            log.info("Adding new genre: {}", request.getName());
            var genre = genreRepository.findByName(request.getName());
            if (genre != null) {
                log.warn("Genre {} already exists", request.getName());
                throw new AppException(ErrorCode.GENRE_ALREADY_EXISTS);
            }
            var newGenre = Genre.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .icon(request.getIcon())
                    .build();
            genreRepository.save(newGenre);
            log.info("Genre {} added successfully", request.getName());
            return GenreResponse.builder()
                    .genreId(newGenre.getGenreId())
                    .name(newGenre.getName())
                    .description(newGenre.getDescription())
                    .icon(newGenre.getIcon())
                    .build();

        }
        catch (Exception e) {
            log.error("Error adding genre: {}", e.getMessage());
            throw e;
        }
    }

    public List<GenreResponse> getAllGenres() {
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
            return genreResponses;
        }
        catch (Exception e) {
            log.error("Error fetching genres: {}", e.getMessage());
            throw e;
        }
    }

    public Page<StoryResponse> searchByGenre(
            String genreId, int page, int size) {
        try {
            log.info("Searching stories by genre: {}", genreId);
            if(!genreRepository.existsById(genreId)) {
                log.warn("Genre with id {} not found", genreId);
                throw new AppException(ErrorCode.GENRE_NOT_FOUND);
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

            log.info("Found {} stories for genre {}", storyResponses.getTotalElements(), genreId);
            return storyResponses;
        }
        catch (Exception e) {
            log.error("Error searching stories by genre: {}", e.getMessage());
            throw e;
        }

    }
}
