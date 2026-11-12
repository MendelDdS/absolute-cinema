package com.cinema.catalog.domain.model.session;

import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.room.Seat;
import com.cinema.catalog.domain.model.shared.Money;

import java.time.Instant;
import java.util.List;

public record SessionScheduled(
        SessionId sessionId,
        MovieId movieId,
        RoomId roomId,
        Instant startsAt,
        Instant endsAt,
        Money price,
        List<String> seatCodes,
        Instant occurredAt
) {
    public SessionScheduled {
        seatCodes = List.copyOf(seatCodes);
    }

    public static SessionScheduled of(Session session, Room room, Instant occurredAt) {
        return new SessionScheduled(
                session.getId(),
                session.getMovieId(),
                session.getRoomId(),
                session.getStartsAt(),
                session.getEndsAt(),
                session.getPrice(),
                room.getSeats().stream().map(Seat::code).toList(),
                occurredAt);
    }
}