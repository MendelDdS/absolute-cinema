package com.cinema.catalog.adapter.out.persistence;

import com.cinema.catalog.adapter.out.persistence.mapper.MoviePersistenceMapper;
import com.cinema.catalog.adapter.out.persistence.repository.MovieJpaRepository;
import com.cinema.catalog.application.port.out.MovieRepository;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.movie.MovieId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MoviePersistenceAdapter implements MovieRepository {

    private final MovieJpaRepository jpaRepository;
    private final MoviePersistenceMapper mapper;


    public MoviePersistenceAdapter(MovieJpaRepository jpaRepository, MoviePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Movie movie) {
        jpaRepository.save(mapper.toEntity(movie));
    }

    @Override
    public Optional<Movie> findById(MovieId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Movie> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
