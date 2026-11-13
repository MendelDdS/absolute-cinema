package com.cinema.catalog.adapter.in.web;

import com.cinema.catalog.application.port.in.CreateMovieUseCase;
import com.cinema.catalog.application.port.in.query.MovieQueryUseCase;
import com.cinema.catalog.application.query.MovieView;
import com.cinema.catalog.domain.model.movie.MovieId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/movies")
class MovieController {

    private final CreateMovieUseCase createMovie;
    private final MovieQueryUseCase movieQuery;

    public MovieController(CreateMovieUseCase createMovie, MovieQueryUseCase movieQuery) {
        this.createMovie = createMovie;
        this.movieQuery = movieQuery;
    }

    @PostMapping
    ResponseEntity<Void> create(@RequestBody CreateMovieRequest request) {
        MovieId id = createMovie.create(request.toCommand());
        return ResponseEntity.created(URI.create("/movies/" + id.value())).build();
    }

    @GetMapping
    ResponseEntity<List<MovieView>> list() {
        List<MovieView> movieList = movieQuery.list();
        return ResponseEntity.ok(movieList);
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<MovieView> findMovie(@PathVariable UUID id) {
        MovieView movie = movieQuery.get(new MovieId(id));
        return ResponseEntity.ok(movie);
    }
}
