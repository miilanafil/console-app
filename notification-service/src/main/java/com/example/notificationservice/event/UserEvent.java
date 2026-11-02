package com.example.notificationservice.event;

public record UserEvent(OperationType operation, String email) {
}