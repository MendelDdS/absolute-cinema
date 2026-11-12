package com.cinema.catalog.application.port.out.query;

import com.cinema.catalog.application.query.MovieView;
import com.cinema.catalog.domain.model.movie.MovieId;

import java.util.List;
import java.util.Optional;

public interface MovieQueryRepository {
    List<MovieView> findAll();
    Optional<MovieView> findById(MovieId id);
}
