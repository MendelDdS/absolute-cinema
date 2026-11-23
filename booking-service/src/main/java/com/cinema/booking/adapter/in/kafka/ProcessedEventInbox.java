package com.cinema.booking.adapter.in.kafka;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class ProcessedEventInbox {

    private final JdbcTemplate jdbcTemplate;

    ProcessedEventInbox(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Marca o evento como processado. Retorna false se ele já tinha sido. */
    boolean markProcessed(UUID eventId) {
        int inserted = jdbcTemplate.update(
                "INSERT INTO processed_events (event_id) VALUES (?) ON CONFLICT (event_id) DO NOTHING",
                eventId);
        return inserted == 1;
    }
}
