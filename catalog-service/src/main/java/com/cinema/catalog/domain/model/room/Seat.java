package com.cinema.catalog.domain.model.room;

import com.cinema.catalog.domain.exception.DomainException;

public record Seat(String row, int number) {
    public Seat {
        if (row == null || !row.matches("[A-Z]")) {
            throw new DomainException("Fileira deve ser uma letra de A a Z");
        }
        if (number < 1) {
            throw new DomainException("Número do assento deve ser maior que zero");
        }
    }

    public String code() {
        return row + number;
    }
}
