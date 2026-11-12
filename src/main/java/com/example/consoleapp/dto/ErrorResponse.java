package com.example.consoleapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Ответ с описанием ошибки")
public record ErrorResponse(

        @Schema(description = "HTTP-код ошибки", example = "400")
        int status,

        @Schema(description = "Что пошло не так", example = "email: Некорректный формат email")
        String message,

        @Schema(description = "Время, когда произошла ошибка", example = "2026-11-12T14:30:00Z")
        Instant timestamp
) {}