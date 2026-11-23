package com.cinema.booking.adapter.out.persistence;

import com.cinema.booking.IntegrationTest;
import com.cinema.booking.application.port.out.SessionRepository;
import com.cinema.booking.domain.model.session.SeatCode;
import com.cinema.booking.domain.model.session.Session;
import com.cinema.booking.domain.model.session.SessionId;
import com.cinema.booking.domain.model.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SessionPersistenceAdapterIntegrationTest extends IntegrationTest {

    private static final Instant STARTS_AT = Instant.parse("2030-01-10T22:00:00Z");

    @Autowired
    private SessionRepository sessions;

    @Test
    @DisplayName("deve salvar e ler a sessão mantendo a ordem dos assentos")
    void savesAndReadsSessionKeepingSeatOrder() {
        SessionId id = new SessionId(UUID.randomUUID());
        sessions.save(Session.register(id, STARTS_AT, Money.of("30.00"),
                List.of(new SeatCode("B1"), new SeatCode("A1"), new SeatCode("A2"))));

        Session found = sessions.findById(id).orElseThrow();

        assertThat(found.getStartsAt()).isEqualTo(STARTS_AT);
        assertThat(found.getPrice()).isEqualTo(Money.of("30.00"));
        assertThat(found.getSeats())
                .extracting(SeatCode::value)
                .containsExactly("B1", "A1", "A2");
    }
}
