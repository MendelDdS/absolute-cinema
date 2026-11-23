package com.cinema.booking.adapter.in.kafka.message;

import com.cinema.booking.application.port.in.RegisterSessionCommand;

import java.math.BigDecimal;
import java.time.Instant;
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

    public RegisterSessionCommand toCommand() {
        return new RegisterSessionCommand(sessionId, Instant.parse(startsAt), price, seatCodes);
    }
}
