package com.cinema.catalog.application.port.out;

import com.cinema.catalog.domain.model.session.SessionScheduled;

public interface SessionEventPublisher {
    void publish(SessionScheduled event);
}