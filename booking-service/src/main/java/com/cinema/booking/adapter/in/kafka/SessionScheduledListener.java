package com.cinema.booking.adapter.in.kafka;

import com.cinema.booking.adapter.in.kafka.message.SessionScheduledMessage;
import com.cinema.booking.application.port.in.RegisterSessionUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class SessionScheduledListener {
    private static final Logger log = LoggerFactory.getLogger(SessionScheduledListener.class);

    private final RegisterSessionUseCase registerSession;
    private final JsonMapper jsonMapper;
    private final ProcessedEventInbox inbox;

    public SessionScheduledListener(RegisterSessionUseCase registerSession, JsonMapper jsonMapper, ProcessedEventInbox inbox) {
        this.registerSession = registerSession;
        this.jsonMapper = jsonMapper;
        this.inbox = inbox;
    }

    @KafkaListener(topics = KafkaTopics.SESSION_SCHEDULED)
    @Transactional
    public void onSessionScheduled(ConsumerRecord<String, String> record) {
        UUID eventId = eventIdOf(record);
        if (!inbox.markProcessed(eventId)) {
            log.info("Evento {} já processado; ignorado", eventId);
            return;
        }
        SessionScheduledMessage message = jsonMapper.readValue(record.value(), SessionScheduledMessage.class);
        registerSession.register(message.toCommand());
    }

    private static UUID eventIdOf(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader("eventId");
        if (header == null) {
            throw new IllegalArgumentException("Mensagem sem header eventId");
        }
        return UUID.fromString(new String(header.value(), StandardCharsets.UTF_8));
    }
}
