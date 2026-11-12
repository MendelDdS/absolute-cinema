package com.cinema.catalog.adapter.out.persistence;

import com.cinema.catalog.application.port.out.SessionRepository;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.session.Session;

import java.time.Instant;
import java.util.List;

public class SessionPersistenceAdapter implements SessionRepository {

    @Override
    public void save(Session session) {

    }

    @Override
    public List<Session> findByRoomIdOverlapping(RoomId roomId, Instant from, Instant to) {
        return List.of();
    }
}
