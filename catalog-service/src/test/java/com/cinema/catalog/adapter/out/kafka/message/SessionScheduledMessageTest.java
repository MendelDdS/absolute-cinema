package com.cinema.catalog.adapter.out.kafka.message;

import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.session.SessionId;
import com.cinema.catalog.domain.model.session.SessionScheduled;
import com.cinema.catalog.domain.model.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SessionScheduledMessageTest {
    @Test
    @DisplayName("deve converter o evento de domínio no contrato da mensagem")
    void convertsEventToMessage() {
        UUID sessionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID movieId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID roomId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        SessionScheduled event = new SessionScheduled(
                new SessionId(sessionId),
                new MovieId(movieId),
                new RoomId(roomId),
                Instant.parse("2026-12-05T22:00:00Z"),
                Instant.parse("2026-12-06T00:15:00Z"),
                Money.of("30"),
                List.of("A1", "A2"),
                Instant.parse("2026-11-20T12:00:00Z"));

        SessionScheduledMessage message = SessionScheduledMessage.from(event);

        assertThat(message.sessionId()).isEqualTo(sessionId);
        assertThat(message.movieId()).isEqualTo(movieId);
        assertThat(message.roomId()).isEqualTo(roomId);
        assertThat(message.startsAt()).isEqualTo("2026-12-05T22:00:00Z");
        assertThat(message.endsAt()).isEqualTo("2026-12-06T00:15:00Z");
        assertThat(message.price()).isEqualTo(new BigDecimal("30.00"));
        assertThat(message.seatCodes()).containsExactly("A1", "A2");
        assertThat(message.occurredAt()).isEqualTo("2026-11-20T12:00:00Z");
    }
}
