package com.example.consoleapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Пользователь")
public record UserDto(

        @Schema(description = "Идентификатор пользователя", example = "1")
        Long id,

        @Schema(description = "Имя пользователя", example = "Иван Петров")
        String name,

        @Schema(description = "Адрес электронной почты", example = "ivan.petrov@example.com")
        String email,

        @Schema(description = "Возраст в годах", example = "25")
        Integer age,

        @Schema(description = "Дата и время создания пользователя", example = "2026-11-12T14:30:00Z")
        Instant createdCode
) { }