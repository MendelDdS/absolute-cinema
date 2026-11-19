package com.cinema.booking.domain.model.session;

import com.cinema.booking.domain.exception.DomainException;
import com.cinema.booking.domain.model.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionTest {

    private static final Instant STARTS_AT = Instant.parse("2030-01-10T22:00:00Z");
    private static final Money PRICE = Money.of("30.00");

    @Test
    @DisplayName("deve registrar assentos mantendo a ordem de cadastro")
    void registersSessionKeepingSeatOrder() {
        Session session = Session.register(
                new SessionId(UUID.randomUUID()),
                STARTS_AT,
                PRICE,
                List.of(new SeatCode("B1"), new SeatCode("A1"), new SeatCode("A2"))
        );

        assertThat(session.getSeats())
                .extracting(SeatCode::value)
                .containsExactly("B1", "A1", "A2");
    }

    @Test
    @DisplayName("deve rejeitar sessão sem assentos")
    void rejectsSessionWithoutSeats() {
        assertThatThrownBy(() -> Session.register(
                new SessionId(UUID.randomUUID()),
                STARTS_AT,
                PRICE,
                List.of()))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("deve rejeitar sessão com assentos duplicados")
    void rejectsDuplicateSeats() {
        assertThatThrownBy(() -> Session.register(
                new SessionId(UUID.randomUUID()),
                STARTS_AT,
                PRICE,
                List.of(new SeatCode("A1"), new SeatCode("A1"), new SeatCode("A3"))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("repetidos");
    }
}
