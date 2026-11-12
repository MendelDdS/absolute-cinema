package com.cinema.catalog.domain.model.movie;

import java.util.Objects;
import java.util.UUID;

public record MovieId(UUID value) {
    public MovieId {
        Objects.requireNonNull(value, "MovieId não pode ser nulo");
    }

    public static MovieId generate() {
        return new MovieId(UUID.randomUUID());
    }
}
