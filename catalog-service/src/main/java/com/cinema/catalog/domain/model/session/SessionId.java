package com.cinema.catalog.domain.model.session;

import java.util.Objects;
import java.util.UUID;

public record SessionId(UUID value) {
    public SessionId {
        Objects.requireNonNull(value, "SessionId não pode ser nulo");
    }

    public static SessionId generate() {
        return new SessionId(UUID.randomUUID());
    }
}
