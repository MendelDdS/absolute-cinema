package com.cinema.booking.domain.model.session;

import com.cinema.booking.domain.exception.DomainException;

public record SeatCode(String value) {
    public SeatCode {
        if (value == null || !value.matches("[A-Z][1-9][0-9]*")) {
            throw new DomainException("Código de assento inválido: " + value);
        }
    }
}
