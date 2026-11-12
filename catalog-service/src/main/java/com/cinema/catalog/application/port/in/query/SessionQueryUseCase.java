package com.cinema.catalog.application.port.in.query;

import com.cinema.catalog.application.query.SessionView;
import com.cinema.catalog.domain.model.session.SessionId;

import java.util.List;

public interface SessionQueryUseCase {
    List<SessionView> list(SessionFilter filter);
    SessionView get(SessionId id);
}
