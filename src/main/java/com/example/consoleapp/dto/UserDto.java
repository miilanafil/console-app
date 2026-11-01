package com.example.consoleapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Builder


public record UserDto(
        Long id,
        String name,
        String email,
        Integer age,
        Instant createdCode
) {

    private Long id;
    private String name;
    private String email;
    private Integer age;
    private Instant createdAt;
}