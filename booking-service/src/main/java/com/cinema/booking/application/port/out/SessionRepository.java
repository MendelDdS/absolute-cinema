package com.cinema.booking.application.port.out;

import com.cinema.booking.domain.model.session.Session;
import com.cinema.booking.domain.model.session.SessionId;

import java.util.Optional;

public interface SessionRepository {
    void save(Session session);

    Optional<Session> findById(SessionId id);
}
