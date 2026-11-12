package com.cinema.catalog.application.service.query;

import com.cinema.catalog.application.port.in.query.SessionFilter;
import com.cinema.catalog.application.port.out.query.SessionQueryRepository;
import com.cinema.catalog.application.query.SessionView;
import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.session.SessionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SessionQueryServiceTest {

    // Fake que só guarda os parâmetros recebidos
    static class CapturingRepository implements SessionQueryRepository {
        Instant from;
        Instant to;

        @Override
        public List<SessionView> findStartingBetween(MovieId movieId, Instant from, Instant to) {
            this.from = from;
            this.to = to;
            return List.of();
        }

        @Override
        public Optional<SessionView> findById(SessionId id) {
            return Optional.empty();
        }
    }

    @Test
    @DisplayName("o dia deve ser calculado no fuso do cinema, não em UTC")
    void convertsDateUsingCinemaZone() {
        var repository = new CapturingRepository();
        var service = new SessionQueryService(repository, ZoneId.of("America/Fortaleza"));

        service.list(new SessionFilter(null, LocalDate.of(2026, 11, 20)));

        assertThat(repository.from).isEqualTo(Instant.parse("2026-11-20T03:00:00Z"));
        assertThat(repository.to).isEqualTo(Instant.parse("2026-11-21T03:00:00Z"));
    }
}