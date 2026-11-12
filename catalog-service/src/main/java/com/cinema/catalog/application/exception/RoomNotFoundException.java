package com.cinema.catalog.application.exception;

import com.cinema.catalog.domain.model.room.RoomId;

public class RoomNotFoundException extends NotFoundException {
    public RoomNotFoundException(RoomId id) {
        super("Sala não encontrada: " + id.value());
    }
}