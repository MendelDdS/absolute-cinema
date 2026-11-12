package com.cinema.catalog.application.port.in;

import com.cinema.catalog.domain.model.movie.MovieId;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.shared.Money;
import java.time.Instant;
import java.util.Objects;

public record ScheduleSessionCommand(MovieId movieId, RoomId roomId, Instant startsAt, Money price) {
    public ScheduleSessionCommand {
        Objects.requireNonNull(movieId, "movieId é obrigatório");
        Objects.requireNonNull(roomId, "roomId é obrigatório");
        Objects.requireNonNull(startsAt, "startsAt é obrigatório");
        Objects.requireNonNull(price, "price é obrigatório");
    }
}