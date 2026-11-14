package com.cinema.catalog.adapter.in.web;

import com.cinema.catalog.application.port.in.ScheduleSessionCommand;
import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.shared.Money;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduleSessionRequest(@NotNull UUID movieId,
                                     @NotNull UUID roomId,
                                     @NotNull OffsetDateTime startsAt,
                                     @NotNull BigDecimal price) {

    ScheduleSessionCommand toCommand() {
        return new ScheduleSessionCommand(
                new MovieId(movieId),
                new RoomId(roomId),
                startsAt.toInstant(),
                new Money(price)
        );
    }
}
