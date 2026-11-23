package com.cinema.booking.adapter.in.kafka;
import com.cinema.booking.IntegrationTest;
import com.cinema.booking.application.port.out.SessionRepository;
import com.cinema.booking.domain.model.session.SeatCode;
import com.cinema.booking.domain.model.session.Session;
import com.cinema.booking.domain.model.session.SessionId;
import com.cinema.booking.domain.model.shared.Money;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class SessionScheduledListenerIntegrationTest extends IntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private SessionRepository sessions;

    @Test
    @DisplayName("deve registrar a sessão recebida pelo evento")
    void registersSessionFromEvent() {
        UUID sessionId = UUID.randomUUID();

        send(UUID.randomUUID(), sessionId, "30.00");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(sessions.findById(new SessionId(sessionId))).isPresent());

        Session session = sessions.findById(new SessionId(sessionId)).orElseThrow();
        assertThat(session.getPrice()).isEqualTo(Money.of("30.00"));
        assertThat(session.getSeats()).extracting(SeatCode::value).containsExactly("B1", "A1", "A2");
    }

    @Test
    @DisplayName("deve ignorar evento repetido")
    void ignoresDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID markerSessionId = UUID.randomUUID();

        send(eventId, sessionId, "30.00");
        send(eventId, sessionId, "99.00");
        send(UUID.randomUUID(), markerSessionId, "10.00");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(sessions.findById(new SessionId(markerSessionId))).isPresent());

        assertThat(sessions.findById(new SessionId(sessionId)).orElseThrow().getPrice())
                .isEqualTo(Money.of("30.00"));
    }

    private void send(UUID eventId, UUID sessionId, String price) {
        String payload = """
                {"sessionId":"%s","movieId":"%s","roomId":"%s",\
                "startsAt":"2030-01-10T22:00:00Z","endsAt":"2030-01-11T00:15:00Z",\
                "price":%s,"seatCodes":["B1","A1","A2"],"occurredAt":"2030-01-01T12:00:00Z"}"""
                .formatted(sessionId, UUID.randomUUID(), UUID.randomUUID(), price);

        ProducerRecord<String, String> record =
                new ProducerRecord<>(KafkaTopics.SESSION_SCHEDULED, sessionId.toString(), payload);
        record.headers().add("eventId", eventId.toString().getBytes(StandardCharsets.UTF_8));
        record.headers().add("eventType", "SessionScheduled".getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(record).join();
    }
}
