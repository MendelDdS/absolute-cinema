package com.cinema.catalog.domain.model.room;

import java.util.Objects;
import java.util.UUID;

public record RoomId(UUID value) {
    public RoomId {
        Objects.requireNonNull(value, "RoomId não pode ser nulo");
    }

    public static RoomId generate() {
        return new RoomId(UUID.randomUUID());
    }
}
