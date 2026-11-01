package com.example.consoleapp.controller;

import com.example.consoleapp.dto.UserDto;
import com.example.consoleapp.dto.UserRequestDto;
import com.example.consoleapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // Используем MockBean для Spring Boot < 3.4
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // Подменяем реальный сервис заглушкой
    private UserService userService;

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        // Данные, которые якобы вернет наш сервис (UserDto)
        UserDto user1 = new UserDto(1L, "Milana", "milana@example.com", 20, LocalDateTime.now());
        UserDto user2 = new UserDto(2L, "Ivan", "ivan@example.com", 25, LocalDateTime.now());

        Mockito.when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        // Выполняем запрос и проверяем, что контроллер отдал их как положено
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2)) // Ожидаем 2 элемента в массиве
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Milana"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Ivan"));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        Long userId = 1L;
        // Данные для заглушки сервиса (UserDto)
        UserDto mockServiceResponse = new UserDto(userId, "Milana", "milana@example.com", 20, LocalDateTime.now());

        Mockito.when(userService.getUserById(userId)).thenReturn(mockServiceResponse);

        // Проверяем GET эндпоинт контроллера
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Milana"))
                .andExpect(jsonPath("$.email").value("milana@example.com"))
                .andExpect(jsonPath("$.createdAt").exists()); // Проверяем, что поле даты присутствует в ответе
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        // То, что клиент посылает в теле POST-запроса
        UserRequestDto requestDto = new UserRequestDto("Yana", "yana@example.com", 22);

        // То, что сервис вернет обратно контроллеру после "сохранения" в БД (UserDto)
        UserDto savedUserDto = new UserDto(3L, "Yana", "yana@example.com", 22, LocalDateTime.now());

        // Контроллер внутри вызывает convertToUserDto(requestDto), поэтому мокаем с Mockito.any()
        Mockito.when(userService.createUser(Mockito.any(UserDto.class))).thenReturn(savedUserDto);

        // Выполняем POST запрос
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // Проверяем статус 201 Created
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Yana"))
                .andExpect(jsonPath("$.email").value("yana@example.com"));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        Long userId = 1L;

        // Метод void ничего не возвращает, мы просто проверяем, что он вызывается без ошибок
        Mockito.doNothing().when(userService.deleteUser(userId));

        // Выполняем DELETE запрос
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent()); // Ожидаем HTTP 204 No Content
    }
}