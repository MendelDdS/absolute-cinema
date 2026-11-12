package com.cinema.catalog.application.exception;

import com.cinema.catalog.domain.model.movie.MovieId;

public class MovieNotFoundException extends NotFoundException {
    public MovieNotFoundException(MovieId id) {
        super("Filme não encontrado: " + id.value());
    }
}
