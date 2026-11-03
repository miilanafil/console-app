package com.example.notificationservice.consumer;

import com.example.notificationservice.event.UserEvent;
import com.example.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    private final NotificationService notificationService;

    public UserEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${notification.kafka.topic}")
    public void onUserEvent(UserEvent event) {
        log.debug("Получено событие: {} для {}", event.operation(), event.email());
        notificationService.send(event);
    }
}