package com.cinema.booking.application.fake;

import com.cinema.booking.application.port.out.SessionRepository;
import com.cinema.booking.domain.model.session.Session;
import com.cinema.booking.domain.model.session.SessionId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemorySessionRepository implements SessionRepository {

    private final Map<SessionId, Session> store = new HashMap<>();

    @Override
    public void save(Session session) {
        store.put(session.getId(), session);
    }

    @Override
    public Optional<Session> findById(SessionId id) {
        return Optional.ofNullable(store.get(id));
    }

    public int count() {
        return store.size();
    }
}
