package com.cinema.catalog.application.port.in;

import com.cinema.catalog.domain.model.room.RoomId;

public interface CreateRoomUseCase {
    RoomId create(CreateRoomCommand command);
}