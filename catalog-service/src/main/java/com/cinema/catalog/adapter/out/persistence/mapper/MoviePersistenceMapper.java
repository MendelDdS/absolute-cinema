package com.cinema.catalog.adapter.out.persistence.mapper;

import com.cinema.catalog.adapter.out.persistence.entity.MovieJpaEntity;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.movie.MovieId;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MoviePersistenceMapper {

    public MovieJpaEntity toEntity(Movie movie) {
        return new MovieJpaEntity(
                movie.getId().value(),                       // MovieId  → UUID
                movie.getTitle(),
                (int) movie.getDuration().toMinutes());      // Duration → int
    }

    public Movie toDomain(MovieJpaEntity entity) {
        return Movie.restore(
                new MovieId(entity.getId()),                 // UUID → MovieId
                entity.getTitle(),
                Duration.ofMinutes(entity.getDurationMinutes()));  // int → Duration
    }
}
