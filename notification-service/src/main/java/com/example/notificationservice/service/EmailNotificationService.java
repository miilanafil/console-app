package com.example.notificationservice.service;

import com.example.notificationservice.event.OperationType;
import com.example.notificationservice.event.UserEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    private static final String SUBJECT = "Уведомление об аккаунте";
    private static final String CREATED_TEXT = "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
    private static final String DELETED_TEXT = "Здравствуйте! Ваш аккаунт был удалён.";

    private final JavaMailSender mailSender;
    private final String from;

    public EmailNotificationService(JavaMailSender mailSender,
                                    @Value("${notification.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(UserEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(event.email());
        message.setSubject(SUBJECT);
        message.setText(textFor(event.operation()));
        mailSender.send(message);
    }

    private String textFor(OperationType operation) {
        return switch (operation) {
            case CREATED -> CREATED_TEXT;
            case DELETED -> DELETED_TEXT;
        };
    }
}