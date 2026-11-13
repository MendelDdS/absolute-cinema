package com.cinema.catalog.application.port.out;

import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.room.RoomId;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    void save(Room room);
    Optional<Room> findById(RoomId id);
    List<Room> findAll();
}