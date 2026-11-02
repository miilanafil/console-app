package com.example.notificationservice.service;

import com.example.notificationservice.event.UserEvent;

public interface NotificationService {

    void send(UserEvent event);
}