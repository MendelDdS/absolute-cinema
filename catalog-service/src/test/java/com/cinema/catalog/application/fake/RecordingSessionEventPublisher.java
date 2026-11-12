package com.cinema.catalog.application.fake;

import com.cinema.catalog.application.port.out.SessionEventPublisher;
import com.cinema.catalog.domain.model.session.SessionScheduled;

import java.util.ArrayList;
import java.util.List;

public class RecordingSessionEventPublisher implements SessionEventPublisher {

    private final List<SessionScheduled> published = new ArrayList<>();

    @Override
    public void publish(SessionScheduled event) {
        published.add(event);
    }

    public List<SessionScheduled> published() {
        return published;
    }
}