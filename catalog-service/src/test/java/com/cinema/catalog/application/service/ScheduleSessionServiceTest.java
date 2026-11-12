package com.cinema.catalog.application.service;

import com.cinema.catalog.application.exception.MovieNotFoundException;
import com.cinema.catalog.application.exception.RoomNotFoundException;
import com.cinema.catalog.application.fake.*;
import com.cinema.catalog.application.port.in.ScheduleSessionCommand;
import com.cinema.catalog.domain.exception.SessionOverlapException;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.session.SessionId;
import com.cinema.catalog.domain.model.session.SessionScheduled;
import com.cinema.catalog.domain.model.shared.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

class ScheduleSessionServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");
    private static final Money PRICE = Money.of("30.00");

    private final InMemoryMovieRepository movies = new InMemoryMovieRepository();
    private final InMemoryRoomRepository rooms = new InMemoryRoomRepository();
    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final RecordingSessionEventPublisher publisher = new RecordingSessionEventPublisher();
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final ScheduleSessionService service =
            new ScheduleSessionService(movies, rooms, sessions, publisher, clock);

    private Movie movie;
    private Room room;

    @BeforeEach
    void setUp() {
        movie = Movie.create("Duna", Duration.ofMinutes(120));   // ocupa 2h15 com limpeza
        room = Room.withGrid("Sala 1", 5, 10);
        movies.save(movie);
        rooms.save(room);
    }

    private ScheduleSessionCommand commandAt(RoomId roomId, String startsAt) {
        return new ScheduleSessionCommand(movie.getId(), roomId, Instant.parse(startsAt), PRICE);
    }

    @Test
    @DisplayName("deve salvar a sessão e publicar o evento")
    void schedulesSession() {
        SessionId id = service.schedule(commandAt(room.getId(), "2026-11-20T14:00:00Z"));

        assertThat(sessions.findAll()).extracting(s -> s.getId()).containsExactly(id);

        assertThat(publisher.published()).hasSize(1);
        SessionScheduled event = publisher.published().getFirst();
        assertThat(event.sessionId()).isEqualTo(id);
        assertThat(event.seatCodes()).hasSize(50);
        assertThat(event.occurredAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("deve falhar quando o filme não existe, sem salvar nem publicar")
    void failsWhenMovieNotFound() {
        var command = new ScheduleSessionCommand(
                MovieId.generate(), room.getId(), Instant.parse("2026-11-20T14:00:00Z"), PRICE);

        assertThatThrownBy(() -> service.schedule(command))
                .isInstanceOf(MovieNotFoundException.class);

        assertThat(sessions.findAll()).isEmpty();
        assertThat(publisher.published()).isEmpty();
    }

    @Test
    @DisplayName("deve falhar quando a sala não existe")
    void failsWhenRoomNotFound() {
        assertThatThrownBy(() -> service.schedule(commandAt(RoomId.generate(), "2026-11-20T14:00:00Z")))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    @DisplayName("deve rejeitar sessão sobreposta, sem salvar nem publicar a nova")
    void rejectsOverlap() {
        service.schedule(commandAt(room.getId(), "2026-11-20T14:00:00Z"));   // 14:00–16:15

        assertThatThrownBy(() -> service.schedule(commandAt(room.getId(), "2026-11-20T15:00:00Z")))
                .isInstanceOf(SessionOverlapException.class);

        assertThat(sessions.findAll()).hasSize(1);
        assertThat(publisher.published()).hasSize(1);
    }

    @Test
    @DisplayName("deve permitir o mesmo horário em outra sala")
    void allowsSameTimeInAnotherRoom() {
        Room otherRoom = Room.withGrid("Sala 2", 5, 10);
        rooms.save(otherRoom);

        service.schedule(commandAt(room.getId(), "2026-11-20T14:00:00Z"));
        service.schedule(commandAt(otherRoom.getId(), "2026-11-20T14:00:00Z"));

        assertThat(sessions.findAll()).hasSize(2);
    }
}