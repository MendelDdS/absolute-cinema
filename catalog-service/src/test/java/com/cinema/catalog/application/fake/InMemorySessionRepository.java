package com.cinema.catalog.application.fake;

import com.cinema.catalog.application.port.out.SessionRepository;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.session.Session;
import com.cinema.catalog.domain.model.session.SessionId;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemorySessionRepository implements SessionRepository {
    private final Map<SessionId, Session> store = new HashMap<>();

    @Override
    public void save(Session session) {
        store.put(session.getId(), session);
    }

    @Override
    public List<Session> findByRoomIdOverlapping(RoomId roomId, Instant from, Instant to) {
        return store.values().stream()
                .filter((session -> session.getRoomId().equals(roomId)))
                .filter(session -> session.getStartsAt().isBefore(to) && from.isBefore(session.getEndsAt()))
                .toList();
    }

    public Collection<Session> findAll() {
        return store.values();
    }
}
