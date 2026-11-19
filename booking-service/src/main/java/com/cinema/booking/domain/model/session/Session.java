package com.cinema.booking.domain.model.session;

import com.cinema.booking.domain.exception.DomainException;
import com.cinema.booking.domain.model.shared.Money;

import java.time.Instant;
import java.util.*;

public class Session {

    private final SessionId id;
    private final Instant startsAt;
    private final Money price;
    private final Set<SeatCode> seats;

    private Session(SessionId id, Instant startsAt, Money price, List<SeatCode> seats) {
        this.id = Objects.requireNonNull(id);
        this.startsAt = Objects.requireNonNull(startsAt);
        this.price = Objects.requireNonNull(price);
        Objects.requireNonNull(seats);
        if (seats.isEmpty()) {
            throw new DomainException("A sessão deve ter ao menos um assento");
        }
        Set<SeatCode> unique = new LinkedHashSet<>(seats);
        if (unique.size() != seats.size()) {
            throw new DomainException("A sessão não pode ter assentos repetidos");
        }
        this.seats = Collections.unmodifiableSet(unique);
    }

    public static Session register(SessionId id, Instant startsAt, Money price, List<SeatCode> seats) {
        return new Session(id, startsAt, price, seats);
    }

    public static Session restore(SessionId id, Instant startsAt, Money price, List<SeatCode> seats) {
        return new Session(id, startsAt, price, seats);
    }

    public SessionId getId() {
        return id;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Money getPrice() {
        return price;
    }

    public Set<SeatCode> getSeats() {
        return seats;
    }
}
