package com.cinema.booking.domain.model.session;

import java.util.Objects;
import java.util.UUID;

public record SessionId(UUID value) {
    public SessionId {
        Objects.requireNonNull(value, "SessionId não pode ser nulo");
    }
}
