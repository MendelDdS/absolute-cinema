package com.cinema.catalog.application.port.in.query;

import com.cinema.catalog.domain.model.movie.MovieId;

import java.time.LocalDate;
import java.util.Objects;

public record SessionFilter(MovieId movieId, LocalDate date) {
    public SessionFilter {
        Objects.requireNonNull(date, "A data é obrigatória");
    }

    public boolean hasMovie() {
        return movieId != null;
    }
}
