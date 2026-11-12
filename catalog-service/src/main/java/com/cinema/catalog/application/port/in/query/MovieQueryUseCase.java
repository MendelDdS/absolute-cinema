package com.cinema.catalog.application.port.in.query;

import com.cinema.catalog.application.query.MovieView;
import com.cinema.catalog.domain.model.movie.MovieId;

import java.util.List;

public interface MovieQueryUseCase {
    List<MovieView> list();
    MovieView get(MovieId id);
}
