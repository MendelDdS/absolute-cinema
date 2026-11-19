package com.cinema.catalog.adapter.out.persistence;

import com.cinema.catalog.IntegrationTest;
import com.cinema.catalog.application.port.out.MovieRepository;
import com.cinema.catalog.application.port.out.RoomRepository;
import com.cinema.catalog.application.port.out.SessionRepository;
import com.cinema.catalog.domain.exception.SessionOverlapException;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.session.Session;
import com.cinema.catalog.domain.model.session.SessionId;
import com.cinema.catalog.domain.model.shared.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionPersistenceAdapterIntegrationTest extends IntegrationTest {

    private static final Instant START = Instant.parse("2030-01-10T22:00:00Z");

    @Autowired
    private MovieRepository movies;
    @Autowired
    private RoomRepository rooms;
    @Autowired
    private SessionRepository sessions;

    private Movie movie;
    private Room room;

    @BeforeEach
    void setUp() {
        movie = Movie.create("Duna 2", Duration.ofMinutes(120));
        room = Room.withGrid("Sala 1 Teste", 2, 3);
        movies.save(movie);
        rooms.save(room);
    }

    // restore pula a regra do domínio: só a constraint do banco está em ação
    private Session sessionAt(Instant startsAt) {
        return Session.restore(SessionId.generate(), movie.getId(), room.getId(),
                startsAt, startsAt.plus(Duration.ofHours(2)), Money.of("30.00"));
    }

    @Test
    @DisplayName("a constraint deve rejeitar sessão sobreposta, traduzida para SessionOverlapException")
    void rejectsOverlapAtDatabaseLevel() {
        sessions.save(sessionAt(START));

        assertThatThrownBy(() -> sessions.save(sessionAt(START.plus(Duration.ofHours(1)))))
                .isInstanceOf(SessionOverlapException.class);
    }

    @Test
    @DisplayName("a constraint deve aceitar sessão que começa quando a anterior termina")
    void acceptsAdjacentSessionAtDatabaseLevel() {
        sessions.save(sessionAt(START));

        assertThatCode(() -> sessions.save(sessionAt(START.plus(Duration.ofHours(2)))))
                .doesNotThrowAnyException();
    }
}