package com.cinema.catalog.adapter.out.outbox;

import com.cinema.catalog.adapter.out.kafka.KafkaTopics;
import com.cinema.catalog.adapter.out.kafka.message.SessionScheduledMessage;
import com.cinema.catalog.application.port.out.SessionEventPublisher;
import com.cinema.catalog.domain.model.session.SessionScheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Component
class OutboxSessionEventPublisher implements SessionEventPublisher {
    private final OutboxJpaRepository repository;
    private final JsonMapper jsonMapper;

    OutboxSessionEventPublisher(OutboxJpaRepository repository, JsonMapper jsonMapper) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void publish(SessionScheduled event) {
        SessionScheduledMessage message = SessionScheduledMessage.from(event);
        repository.save(new OutboxEventJpaEntity(
                UUID.randomUUID(),
                event.sessionId().value(),
                KafkaTopics.SESSION_SCHEDULED,
                "SessionScheduled",
                jsonMapper.writeValueAsString(message),
                event.occurredAt()));
    }
}
