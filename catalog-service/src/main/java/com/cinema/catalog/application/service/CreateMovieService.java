package com.cinema.catalog.application.service;

import com.cinema.catalog.application.port.in.CreateMovieCommand;
import com.cinema.catalog.application.port.in.CreateMovieUseCase;
import com.cinema.catalog.application.port.out.MovieRepository;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.movie.MovieId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class CreateMovieService implements CreateMovieUseCase {
    private final MovieRepository movieRepository;

    public CreateMovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    @Transactional
    public MovieId create(CreateMovieCommand command) {
        Movie movie = Movie.create(command.title(), Duration.ofMinutes(command.durationMinutes()));
        movieRepository.save(movie);
        return movie.getId();
    }
}
