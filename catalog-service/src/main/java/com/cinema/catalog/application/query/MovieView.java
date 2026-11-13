package com.cinema.catalog.application.query;

import java.util.UUID;

import com.cinema.catalog.domain.model.movie.Movie;

public record MovieView(UUID id, String title, int durationMinutes) {
    public static MovieView from(Movie movie) {
        return new MovieView(
                movie.getId().value(),
                movie.getTitle(),
                (int) movie.getDuration().toMinutes());
    }
}
