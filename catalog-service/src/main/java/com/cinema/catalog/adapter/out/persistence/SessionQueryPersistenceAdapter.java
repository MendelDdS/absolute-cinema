package com.cinema.catalog.adapter.out.persistence;

import com.cinema.catalog.adapter.out.persistence.repository.SessionJpaRepository;
import com.cinema.catalog.application.port.out.query.SessionQueryRepository;
import com.cinema.catalog.application.query.SessionView;
import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.session.SessionId;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class SessionQueryPersistenceAdapter implements SessionQueryRepository {

    private final SessionJpaRepository jpaRepository;

    public SessionQueryPersistenceAdapter(SessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<SessionView> findStartingBetween(MovieId movieId, Instant from, Instant to) {
        if (movieId == null) {
            return jpaRepository.findViewsStartingBetween(from, to);
        }
        return jpaRepository.findViewsByMovieStartingBetween(movieId.value(), from, to);
    }

    @Override
    public Optional<SessionView> findById(SessionId id) {
        return jpaRepository.findViewById(id.value());
    }
}
