package com.example.consoleapp.dto;

//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;

import java.time.Instant;


public record UserResponseDto (
        Long id,
        String name,
        String email,
        Integer age,
        Instant createdAt
    )
{}