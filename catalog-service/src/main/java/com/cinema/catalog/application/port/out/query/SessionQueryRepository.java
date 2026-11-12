package com.cinema.catalog.application.port.out.query;

import com.cinema.catalog.application.query.SessionView;
import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.session.SessionId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SessionQueryRepository {
    List<SessionView> findStartingBetween(MovieId movieId, Instant from, Instant to);
    Optional<SessionView> findById(SessionId id);
}
