package com.cinema.catalog.application.port.in;

import com.cinema.catalog.domain.model.movie.MovieId;

public interface CreateMovieUseCase {
    MovieId create(CreateMovieCommand command);
}
