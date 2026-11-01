package com.example.consoleapp.dto;

import java.time.Instant;

public record UserResponseDto (
        Long id,
        String name,
        String email,
        Integer age,
        Instant createdAt
    )
{}