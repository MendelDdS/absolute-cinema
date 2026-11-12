package com.cinema.catalog.domain.model.movie;

import com.cinema.catalog.domain.exception.DomainException;

import java.time.Duration;
import java.util.Objects;

public class Movie {
    private final MovieId id;
    private final String title;
    private final Duration duration;

    public Movie(MovieId id, String title, Duration duration) {
        if (title == null || title.isBlank()) {
            throw new DomainException("O título do filme é obrigatório");
        }
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new DomainException("A duração do filme deve ser maior que zero");
        }
        this.id = Objects.requireNonNull(id);
        this.title = title.strip();
        this.duration = duration;
    }

    public MovieId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Duration getDuration() {
        return duration;
    }

    public static Movie create(String title, Duration duration) {
        return new Movie(MovieId.generate(), title, duration);
    }

    public static Movie restore(MovieId id, String title, Duration duration) {
        return new Movie(id, title, duration);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Movie other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
