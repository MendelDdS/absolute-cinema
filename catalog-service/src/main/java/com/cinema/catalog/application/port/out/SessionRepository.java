package com.cinema.catalog.application.port.out;

import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.session.Session;
import java.time.Instant;
import java.util.List;

public interface SessionRepository {
    void save(Session session);

    List<Session> findByRoomIdOverlapping(RoomId roomId, Instant from, Instant to);
}