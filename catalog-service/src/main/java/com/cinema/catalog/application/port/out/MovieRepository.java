package com.cinema.catalog.application.port.out;

import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.movie.MovieId;

import java.util.List;
import java.util.Optional;

public interface MovieRepository {
    void save(Movie movie);
    Optional<Movie> findById(MovieId id);
    List<Movie> findAll();
}