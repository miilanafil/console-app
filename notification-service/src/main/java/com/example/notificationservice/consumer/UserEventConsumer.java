package com.example.notificationservice.consumer;

import com.example.notificationservice.event.UserEvent;
import com.example.notificationservice.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public UserEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${notification.kafka.topic}")
    public void onUserEvent(String payload, Acknowledgment acknowledgment) throws JsonProcessingException {
        UserEvent event = objectMapper.readValue(payload, UserEvent.class);
        if (event.operation() == null || event.email() == null || event.email().isBlank()) {
            throw new IllegalArgumentException("В сообщении нет операции или email: " + payload);
        }
        log.debug("Получено событие: {} для {}", event.operation(), event.email());
        notificationService.send(event);
        acknowledgment.acknowledge();
    }
}