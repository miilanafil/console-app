package com.example.consoleapp.controller;

import com.example.consoleapp.dto.UserDto;
import com.example.consoleapp.dto.UserRequestDto;
import com.example.consoleapp.dto.UserResponseDto;
import com.example.consoleapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. Получить всех пользователей
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserDto> oldUsers = userService.getAllUsers();

        // Преобразуем список UserDto в список UserResponseDto
        List<UserResponseDto> responseUsers = oldUsers.stream()
                .map(this::convertToResponseDto)
                .toList();

        return ResponseEntity.ok(responseUsers);
    }

    // 2. Получить пользователя по ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(convertToResponseDto(userDto));
    }

    // 3. Создать нового пользователя
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto requestDto) {
        // Передаем в сервис старый формат, если ваш сервис принимает UserDto
        // Если ваш сервис принимает UserRequestDto, оставьте просто userService.createUser(requestDto)
        UserDto savedOldUser = userService.createUser(convertToUserDto(requestDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedOldUser));
    }

    // 4. Обновить пользователя
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserRequestDto requestDto) {
        UserDto updatedOldUser = userService.updateUser(id, convertToUserDto(requestDto));
        return ResponseEntity.ok(convertToResponseDto(updatedOldUser));
    }

    // 5. Удалить пользователя
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Вспомогательный метод маппинга наружу (UserDto -> UserResponseDto)
    private UserResponseDto convertToResponseDto(UserDto dto) {
        return UserResponseDto.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .age(dto.getAge())
                .createdAt(dto.getCreatedAt()) // Если в UserDto нет createdAt, укажите java.time.LocalDateTime.now()
                .build();
    }

    // Вспомогательный метод маппинга внутрь (UserRequestDto -> UserDto)
    private UserDto convertToUserDto(UserRequestDto request) {
        return UserDto.builder()
                .name(request.getName())
                .email(request.getEmail())
                .age(request.getAge())
                .build();
    }
}
// REST API