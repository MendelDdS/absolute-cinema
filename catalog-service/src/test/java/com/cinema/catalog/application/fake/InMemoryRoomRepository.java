package com.cinema.catalog.application.fake;

import com.cinema.catalog.application.port.out.RoomRepository;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.room.RoomId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryRoomRepository implements RoomRepository {

    private final Map<RoomId, Room> store = new HashMap<>();

    @Override
    public void save(Room room) {
        store.put(room.getId(), room);
    }

    @Override
    public Optional<Room> findById(RoomId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Room> findAll() {
        return store.values().stream().toList();
    }

    public int count() {
        return store.size();
    }
}
