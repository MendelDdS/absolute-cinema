package com.cinema.catalog.adapter.out.kafka.message;

import com.cinema.catalog.domain.model.session.SessionScheduled;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SessionScheduledMessage(
        UUID sessionId,
        UUID movieId,
        UUID roomId,
        String startsAt,
        String endsAt,
        BigDecimal price,
        List<String> seatCodes,
        String occurredAt) {

    public static SessionScheduledMessage from(SessionScheduled event) {
        return new SessionScheduledMessage(
                event.sessionId().value(),
                event.movieId().value(),
                event.roomId().value(),
                event.startsAt().toString(),
                event.endsAt().toString(),
                event.price().amount(),
                event.seatCodes(),
                event.occurredAt().toString()
        );
    }
}
