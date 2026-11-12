package com.cinema.catalog.application.fake;

import com.cinema.catalog.application.port.out.MovieRepository;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.movie.MovieId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryMovieRepository implements MovieRepository {

    private final Map<MovieId, Movie> store = new HashMap<>();

    @Override
    public void save(Movie movie) {
        store.put(movie.getId(), movie);
    }

    @Override
    public Optional<Movie> findById(MovieId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Movie> findAll() {
        return store.values().stream().toList();
    }

    public int count() {
        return store.size();
    }
}
