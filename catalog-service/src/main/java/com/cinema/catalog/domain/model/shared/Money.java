package com.cinema.catalog.domain.model.shared;

import com.cinema.catalog.domain.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money (BigDecimal amount){
    public Money {
        Objects.requireNonNull(amount, "Valor não pode ser nulo");
        if (amount.signum() < 0) {
            throw new DomainException("Valor não pode ser negativo");
        }

        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }
}
