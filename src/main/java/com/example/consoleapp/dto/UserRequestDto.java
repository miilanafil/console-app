package com.example.consoleapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для создания или изменения пользователя")
public record UserRequestDto(

        @Schema(description = "Имя пользователя: только буквы, между ними можно ставить пробел, дефис или апостроф",
                example = "Иван Петров")
        @NotBlank(message = "Имя не может быть пустым")
        @Size(max = 255, message = "Имя не должно превышать 255 символов")
        @Pattern(regexp = "^\\p{L}+(?:[ '-]\\p{L}+)*$", message = "Имя может содержать только буквы, а также пробел, дефис или апостроф между ними")
        String name,

        @Schema(description = "Адрес электронной почты, на него придёт письмо о создании и удалении аккаунта. Должен быть уникальным",
                example = "ivan.petrov@example.com")
        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        @Size(max = 255, message = "Email не должен превышать 255 символов")
        String email,

        @Schema(description = "Возраст в годах, от 1 до 100",
                example = "25")
        @NotNull(message = "Возраст обязателен")
        @Min(value = 1, message = "Возраст должен быть больше 0")
        @Max(value = 100, message = "Возраст не может превышать 100 лет")
        Integer age
) {}