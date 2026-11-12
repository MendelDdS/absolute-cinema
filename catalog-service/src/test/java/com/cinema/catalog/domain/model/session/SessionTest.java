package com.cinema.catalog.domain.model.session;

import com.cinema.catalog.domain.exception.DomainException;
import com.cinema.catalog.domain.exception.SessionOverlapException;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SessionTest {

    // "Agora" fixo: 20/11/2026 12:00 UTC
    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    // Filme de 2h → sessão ocupa 2h15 (com limpeza)
    private final Movie movie = Movie.create("Duna", Duration.ofMinutes(120));
    private final Room room = Room.withGrid("Sala 1", 5, 10);
    private final Money price = Money.of("30.00");

    private Session scheduleAt(Room room, String startsAt) {
        return Session.schedule(movie, room, Instant.parse(startsAt), price, clock);
    }

    @Test
    @DisplayName("deve calcular o término com duração + 15 min de limpeza")
    void calculatesEnd() {
        Session session = scheduleAt(room, "2026-11-20T14:00:00Z");

        assertThat(session.getEndsAt()).isEqualTo(Instant.parse("2026-11-20T16:15:00Z"));
    }

    @Test
    @DisplayName("deve rejeitar sessão no passado")
    void rejectsPast() {
        assertThatThrownBy(() -> scheduleAt(room, "2026-11-20T11:59:00Z"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("futuro");
    }

    @Test
    @DisplayName("deve rejeitar sessão começando exatamente agora")
    void rejectsNow() {
        assertThatThrownBy(() -> scheduleAt(room, "2026-11-20T12:00:00Z"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("restore deve aceitar sessão no passado (dado vindo do banco)")
    void restoreAcceptsPast() {
        assertThatCode(() -> Session.restore(
                SessionId.generate(), movie.getId(), room.getId(),
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T12:15:00Z"),
                price)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve detectar sobreposição parcial na mesma sala")
    void detectsPartialOverlap() {
        Session a = scheduleAt(room, "2026-11-20T14:00:00Z");   // 14:00–16:15
        Session b = scheduleAt(room, "2026-11-20T15:00:00Z");   // 15:00–17:15

        assertThat(a.overlapsWith(b)).isTrue();
        assertThat(b.overlapsWith(a)).isTrue();
    }

    @Test
    @DisplayName("deve detectar sessões no mesmo horário")
    void detectsSameTime() {
        Session a = scheduleAt(room, "2026-11-20T14:00:00Z");
        Session b = scheduleAt(room, "2026-11-20T14:00:00Z");

        assertThat(a.overlapsWith(b)).isTrue();
    }

    @Test
    @DisplayName("sessões encostadas não conflitam")
    void adjacentDoNotOverlap() {
        Session a = scheduleAt(room, "2026-11-20T14:00:00Z");   // termina 16:15
        Session b = scheduleAt(room, "2026-11-20T16:15:00Z");   // começa 16:15

        assertThat(a.overlapsWith(b)).isFalse();
    }

    @Test
    @DisplayName("mesmo horário em salas diferentes não conflita")
    void differentRoomsDoNotOverlap() {
        Room otherRoom = Room.withGrid("Sala 2", 5, 10);
        Session a = scheduleAt(room, "2026-11-20T14:00:00Z");
        Session b = scheduleAt(otherRoom, "2026-11-20T14:00:00Z");

        assertThat(a.overlapsWith(b)).isFalse();
    }

    @Test
    @DisplayName("ensureNoOverlapWith deve lançar exceção quando houver conflito")
    void ensureNoOverlapThrows() {
        Session existing = scheduleAt(room, "2026-11-20T14:00:00Z");
        Session candidate = scheduleAt(room, "2026-11-20T15:00:00Z");

        assertThatThrownBy(() -> candidate.ensureNoOverlapWith(List.of(existing)))
                .isInstanceOf(SessionOverlapException.class);
    }

    @Test
    @DisplayName("ensureNoOverlapWith deve ignorar a própria sessão")
    void ensureNoOverlapIgnoresItself() {
        Session session = scheduleAt(room, "2026-11-20T14:00:00Z");

        assertThatCode(() -> session.ensureNoOverlapWith(List.of(session)))
                .doesNotThrowAnyException();
    }
}