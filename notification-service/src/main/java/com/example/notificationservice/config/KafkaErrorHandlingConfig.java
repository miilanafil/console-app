package com.example.notificationservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfig {

    private static final long RETRY_PAUSE_MS = 1000L;
    private static final long RETRY_COUNT = 2L;
    private static final String RETENTION_SEVEN_DAYS_MS = "604800000";

    @Bean
    public NewTopic userEventsTopic(@Value("${notification.kafka.topic}") String topic) {
        return TopicBuilder.name(topic)
                .partitions(1)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, RETENTION_SEVEN_DAYS_MS)
                .build();
    }

    @Bean
    public NewTopic userEventsFailedTopic(@Value("${notification.kafka.topic}") String topic) {
        return TopicBuilder.name(topic + ".DLT")
                .partitions(1)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, RETENTION_SEVEN_DAYS_MS)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_PAUSE_MS, RETRY_COUNT));
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class, JsonProcessingException.class);
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }
}