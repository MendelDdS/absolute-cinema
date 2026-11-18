package com.cinema.catalog.adapter.out.outbox;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final int BATCH_SIZE = 100;
    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(5);

    private final OutboxJpaRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Clock clock;

    OutboxRelay(OutboxJpaRepository repository, KafkaTemplate<String, String> kafkaTemplate, Clock clock) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPending() {
        for (OutboxEventJpaEntity event : repository.findBatchToPublish(BATCH_SIZE)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    event.getTopic(), event.getAggregateId().toString(), event.getPayload());
            record.headers().add("eventId", event.getId().toString().getBytes(StandardCharsets.UTF_8));
            record.headers().add("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8));

            try {
                kafkaTemplate.send(record).get(SEND_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (TimeoutException | ExecutionException e) {
                log.warn("Falha ao publicar evento {} do outbox; nova tentativa no próximo ciclo", event.getId(), e);
                return;
            }

            event.markPublished(clock.instant());
        }
    }
}
