package com.cinema.booking.application.service;
import com.cinema.booking.application.fake.InMemorySessionRepository;
import com.cinema.booking.application.port.in.RegisterSessionCommand;
import com.cinema.booking.domain.exception.DomainException;
import com.cinema.booking.domain.model.session.SeatCode;
import com.cinema.booking.domain.model.session.Session;
import com.cinema.booking.domain.model.session.SessionId;
import com.cinema.booking.domain.model.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterSessionServiceTest {

    private static final Instant STARTS_AT = Instant.parse("2030-01-10T22:00:00Z");

    private final InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
    private final RegisterSessionService service = new RegisterSessionService(sessionRepository);

    @Test
    @DisplayName("deve salvar a sessão com os dados do comando")
    void registersSession() {
        UUID id = UUID.randomUUID();

        service.register(new RegisterSessionCommand(id, STARTS_AT, new BigDecimal("30.00"), List.of("B1", "A1", "A2")));

        Session saved = sessionRepository.findById(new SessionId(id)).orElseThrow();
        assertThat(saved.getStartsAt()).isEqualTo(STARTS_AT);
        assertThat(saved.getPrice()).isEqualTo(Money.of("30.00"));
        assertThat(saved.getSeats()).extracting(SeatCode::value).containsExactly("B1", "A1", "A2");
    }

    @Test
    @DisplayName("não deve salvar sessão sem assentos")
    void doesNotSaveSessionWithoutSeats() {
        RegisterSessionCommand command = new RegisterSessionCommand(UUID.randomUUID(), STARTS_AT, new BigDecimal("30.00"), List.of());

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ao menos um assento");

        assertThat(sessionRepository.count()).isZero();
    }

    @Test
    @DisplayName("não deve salvar sessão com código de assento inválido")
    void doesNotSaveSessionWithInvalidSeatCode() {
        RegisterSessionCommand command = new RegisterSessionCommand(UUID.randomUUID(), STARTS_AT, new BigDecimal("30.00"), List.of("B1", "a1", "A2"));
        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Código de assento inválido: a1");

        assertThat(sessionRepository.count()).isZero();
    }
}
