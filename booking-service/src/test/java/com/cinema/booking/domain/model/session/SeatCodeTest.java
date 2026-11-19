package com.cinema.booking.domain.model.session;

import com.cinema.booking.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeatCodeTest {
    @ParameterizedTest
    @ValueSource(strings = {"A1", "Z9", "B12", "C100"})
    @DisplayName("deve aceitar códigos no formato letra + número")
    void acceptsValidCodes(String code) {
        assertThatCode(() -> new SeatCode(code)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"a1", "A0", "A01", "1A", "AA1", "A", "A-1"})
    @DisplayName("deve rejeitar códigos fora do formato")
    void rejectsInvalidCodes(String code) {
        assertThatThrownBy(() -> new SeatCode(code)).isInstanceOf(DomainException.class);
    }
}
