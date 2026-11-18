package com.cinema.catalog.config;

import com.cinema.catalog.adapter.out.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class KafkaConfig {

    @Bean
    public NewTopic sessionScheduledTopic() {
        return TopicBuilder.name(KafkaTopics.SESSION_SCHEDULED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
