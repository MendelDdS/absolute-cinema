package com.cinema.catalog.application.service.query;

import com.cinema.catalog.application.exception.MovieNotFoundException;
import com.cinema.catalog.application.port.in.query.MovieQueryUseCase;
import com.cinema.catalog.application.port.out.MovieRepository;
import com.cinema.catalog.application.query.MovieView;
import com.cinema.catalog.domain.model.movie.MovieId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovieQueryService implements MovieQueryUseCase {

    private final MovieRepository movieRepository;

    public MovieQueryService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public List<MovieView> list() {
        return movieRepository.findAll()
                .stream()
                .map(MovieView::from)
                .toList();
    }

    @Override
    public MovieView get(MovieId id) {
        return movieRepository.findById(id)
                .map(MovieView::from)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }
}
