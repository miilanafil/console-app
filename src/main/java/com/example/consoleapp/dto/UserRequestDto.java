package com.example.consoleapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @NotBlank(message = "Имя не может быть пустым")
        @Size(max = 255, message = "Имя не должно превышать 255 символов")
        @Pattern(regexp = "^\\p{L}+(?:[ '-]\\p{L}+)*$", message = "Имя может содержать только буквы, а также пробел, дефис или апостроф между ними")
        String name,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        @Size(max = 255, message = "Email не должен превышать 255 символов")
        String email,

        @NotNull(message = "Возраст обязателен")
        @Min(value = 1, message = "Возраст должен быть больше 0")
        @Max(value = 100, message = "Возраст не может превышать 100 лет")
        Integer age
) {}