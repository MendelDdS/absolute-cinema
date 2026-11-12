package com.cinema.catalog.domain.model.session;

import com.cinema.catalog.domain.exception.DomainException;
import com.cinema.catalog.domain.exception.SessionOverlapException;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.shared.Money;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

public class Session {

    private static final Duration CLEANING_INTERVAL = Duration.ofMinutes(15);

    private final SessionId id;
    private final MovieId movieId;
    private final RoomId roomId;
    private final Instant startsAt;
    private final Instant endsAt;
    private final Money price;

    private Session(SessionId id, MovieId movieId, RoomId roomId,
                    Instant startsAt, Instant endsAt, Money price) {
        this.id = Objects.requireNonNull(id);
        this.movieId = Objects.requireNonNull(movieId);
        this.roomId = Objects.requireNonNull(roomId);
        this.startsAt = Objects.requireNonNull(startsAt);
        this.endsAt = Objects.requireNonNull(endsAt);
        this.price = Objects.requireNonNull(price);
        if (!endsAt.isAfter(startsAt)) {
            throw new DomainException("O término deve ser depois do início");
        }
    }

    public static Session schedule(Movie movie, Room room, Instant startsAt,
                                   Money price, Clock clock) {
        if (!startsAt.isAfter(clock.instant())) {
            throw new DomainException("A sessão deve começar no futuro");
        }
        Instant endsAt = startsAt.plus(movie.getDuration()).plus(CLEANING_INTERVAL);
        return new Session(SessionId.generate(), movie.getId(), room.getId(), startsAt, endsAt, price);
    }

    public static Session restore(SessionId id, MovieId movieId, RoomId roomId,
                                  Instant startsAt, Instant endsAt, Money price) {
        return new Session(id, movieId, roomId, startsAt, endsAt, price);
    }

    public boolean overlapsWith(Session other) {
        return roomId.equals(other.roomId)
                && startsAt.isBefore(other.endsAt)
                && other.startsAt.isBefore(endsAt);
    }

    public void ensureNoOverlapWith(Collection<Session> others) {
        boolean conflict = others.stream()
                .filter(other -> !other.id.equals(this.id))
                .anyMatch(this::overlapsWith);
        if (conflict) {
            throw new SessionOverlapException();
        }
    }

    public SessionId getId() { return id; }
    public MovieId getMovieId() { return movieId; }
    public RoomId getRoomId() { return roomId; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public Money getPrice() { return price; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Session other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}