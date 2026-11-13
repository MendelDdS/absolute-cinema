package com.cinema.catalog.application.query;

import java.util.UUID;

import com.cinema.catalog.domain.model.room.Room;

public record RoomView(UUID id, String name, int capacity) {
    public static RoomView from(Room room) {
        return new RoomView(room.getId().value(), room.getName(), room.capacity());
    }
}
