package com.cinema.catalog.application.service.query;

import com.cinema.catalog.application.exception.MovieNotFoundException;
import com.cinema.catalog.application.port.in.query.MovieQueryUseCase;
import com.cinema.catalog.application.port.out.query.MovieQueryRepository;
import com.cinema.catalog.application.query.MovieView;
import com.cinema.catalog.domain.model.movie.MovieId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovieQueryService implements MovieQueryUseCase {

    private final MovieQueryRepository movieRepository;

    public MovieQueryService(MovieQueryRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public List<MovieView> list() {
        return movieRepository.findAll();
    }

    @Override
    public MovieView get(MovieId id) {
        return movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
    }
}
