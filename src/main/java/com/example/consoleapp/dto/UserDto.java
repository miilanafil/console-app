package com.example.consoleapp.dto;

import java.time.Instant;

public record UserDto(
        Long id,
        String name,
        String email,
        Integer age,
        Instant createdCode
) {}