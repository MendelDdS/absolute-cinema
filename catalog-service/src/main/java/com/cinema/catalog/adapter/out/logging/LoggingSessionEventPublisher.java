package com.cinema.catalog.adapter.out.logging;

import com.cinema.catalog.application.port.out.SessionEventPublisher;
import com.cinema.catalog.domain.model.session.SessionScheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Classe temporária antes da integração com mensageria.
@Component
public class LoggingSessionEventPublisher implements SessionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingSessionEventPublisher.class);

    @Override
    public void publish(SessionScheduled event) {
        log.info("SessionScheduled: sessionId={}, roomId={}, startsAt={}",
                event.sessionId().value(), event.roomId().value(), event.startsAt());
    }
}
