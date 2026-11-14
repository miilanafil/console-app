package com.example.consoleapp.controller;

import com.example.consoleapp.dto.UserDto;
import com.example.consoleapp.dto.UserRequestDto;
import com.example.consoleapp.service.UserService;
import com.example.consoleapp.assembler.UserAssembler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserAssembler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_shouldReturnCollectionModelWithHalLinks() throws Exception { //новый

        UserDto user1 = new UserDto(1L, "Milana", "milana@example.com", 20, java.time.Instant.now());
        UserDto user2 = new UserDto(2L, "Nastya", "Nastya@example.com", 24, java.time.Instant.now());

        Mockito.when (userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$._embedded.userDtoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.userDtoList[0].id", is(1)))
                .andExpect(jsonPath("$._embedded.userDtoList[0].name", is("Milana")))
                .andExpect(jsonPath("$._embedded.userDtoList[0]._links.self.href", endsWith("/api/users/1")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/users")));
    }

    @Test
    void getUserById_shouldReturnEntityModelWithHalLinks() throws Exception { // новый

        Instant fixedTime = java.time.Instant.now();
        UserDto user = new UserDto(1L, "Milana", "milana@example.com", 20, fixedTime);
        Mockito.when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Milana")))
                .andExpect(jsonPath("$.email", is("milana@example.com")))
                .andExpect(jsonPath("$.age", is(20)))
                .andExpect(jsonPath("$.createdCode", is(fixedTime.toString())))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/users/1")));

    }

    /* старый
        @Test
        void getAllUsers_ShouldReturnListOfUsers() throws Exception { // старый getAllUsers

            UserDto user1 = new UserDto(1L, "Milana", "milana@example.com", 20, java.time.Instant.now());
            UserDto user2 = new UserDto(2L, "Ivan", "ivan@example.com", 25, java.time.Instant.now());

            Mockito.when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2)) // Ожидаем 2 элемента в массиве
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("Milana"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].name").value("Ivan"));
        }

    /* старый
        @Test
        void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
            Long userId = 1L;

            UserDto mockServiceResponse = new UserDto(userId, "Milana", "milana@example.com", 20, java.time.Instant.now());

            Mockito.when(userService.getUserById(userId)).thenReturn(mockServiceResponse);

            mockMvc.perform(get("/api/users/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.name").value("Milana"))
                    .andExpect(jsonPath("$.email").value("milana@example.com"))
                    .andExpect(jsonPath("$.createdAt").exists()); // Проверяем, что поле даты присутствует в ответе
        }
    */
    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {

        UserRequestDto requestDto = new UserRequestDto("Nastya", "nastya@example.com", 22); //Это клиент посылает в теле POST-запроса
        UserDto savedUserDto = new UserDto(3L, "Nastya", "nastya@example.com", 22, java.time.Instant.now());  //это сервис вернет контроллеру после сохр. в БД (UserDto)
        Mockito.when(userService.createUser(Mockito.any(UserRequestDto.class))).thenReturn(savedUserDto); //Контроллер внутри вызывает convertToUserDto(requestDto)- мокаем с Mockito.any()

        // Выполняем POST запрос:

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // Проверяем статус 201 Created
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Nastya"))
                .andExpect(jsonPath("$.email").value("nastya@example.com"));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        Long userId = 1L;

        Mockito.doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent()); // Ожидаем HTTP 204 No Content
    }
}