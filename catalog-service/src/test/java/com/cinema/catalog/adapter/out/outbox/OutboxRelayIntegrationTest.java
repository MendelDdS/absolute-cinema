package com.cinema.catalog.adapter.out.outbox;

import com.cinema.catalog.IntegrationTest;
import com.cinema.catalog.adapter.out.kafka.KafkaTopics;
import com.cinema.catalog.application.port.in.ScheduleSessionCommand;
import com.cinema.catalog.application.port.in.ScheduleSessionUseCase;
import com.cinema.catalog.application.port.out.MovieRepository;
import com.cinema.catalog.application.port.out.RoomRepository;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.session.SessionId;
import com.cinema.catalog.domain.model.shared.Money;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class OutboxRelayIntegrationTest extends IntegrationTest {

    @Autowired
    private ScheduleSessionUseCase scheduleSession;
    @Autowired
    private MovieRepository movies;
    @Autowired
    private RoomRepository rooms;
    @Autowired
    private KafkaConnectionDetails kafka;

    @Test
    @DisplayName("sessão agendada deve chegar ao tópico com chave, headers e payload")
    void publishesScheduledSessionToKafka() {
        Movie movie = Movie.create("Duna", Duration.ofMinutes(120));
        Room room = Room.withGrid("Sala 1", 2, 3);
        movies.save(movie);
        rooms.save(room);

        SessionId id = scheduleSession.schedule(new ScheduleSessionCommand(
                movie.getId(), room.getId(), Instant.parse("2030-01-10T22:00:00Z"), Money.of("30.00")));
        String key = id.value().toString();

        Map<String, Object> props = KafkaTestUtils.consumerProps(
                String.join(",", kafka.getBootstrapServers()), "outbox-relay-test");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(KafkaTopics.SESSION_SCHEDULED));
            List<ConsumerRecord<String, String>> received = new ArrayList<>();

            await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
                consumer.poll(Duration.ofMillis(500)).forEach(received::add);
                assertThat(received).anyMatch(r -> key.equals(r.key()));
            });

            ConsumerRecord<String, String> record = received.stream()
                    .filter(r -> key.equals(r.key()))
                    .findFirst()
                    .orElseThrow();

            String eventType = new String(record.headers().lastHeader("eventType").value(), StandardCharsets.UTF_8);
            assertThat(eventType).isEqualTo("SessionScheduled");
            assertThat(record.value()).contains(key).contains("\"seatCodes\"");
        }
    }
}