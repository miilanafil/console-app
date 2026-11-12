package com.example.consoleapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("REST API для управления пользователями: создание, просмотр, изменение и удаление. "
                                + "В ответах есть ссылки для навигации по ресурсам (HATEOAS). "
                                + "При создании и удалении пользователя в Kafka уходит событие, "
                                + "по которому сервис уведомлений отправляет письмо.")
                        .version("1.0.0")
                        .contact(new Contact().name("Филиппович Милана, Волкова Анастасия")))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Локальный запуск")))
                .tags(List.of(new Tag()
                        .name("Пользователи")
                        .description("Создание, просмотр, изменение и удаление пользователей")));
    }
}