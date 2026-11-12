package com.cinema.catalog.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SessionView (
        UUID id,
        UUID movieId,
        String movieTitle,
        UUID roomId,
        String roomName,
        Instant startsAt,
        Instant endsAt,
        BigDecimal price
) {
}
