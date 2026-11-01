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
        // Передаем напрямую requestDto, так как сервис ожидает именно его
        UserDto savedOldUser = userService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedOldUser));
    }

    // 4. Обновить пользователя
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserRequestDto requestDto) {
        // Передаем напрямую id и requestDto в соответствии с интерфейсом сервиса
        UserDto updatedOldUser = userService.updateUser(id, requestDto);
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
                .id(dto.id())
                .name(dto.name())
                .email(dto.email())
                .age(dto.age())
                // Передаем createdCode. Если в UserResponseDto нужен тип LocalDateTime,
                // преобразуем Instant с помощью ofInstant:
                .createdAt(dto.createdCode() )
                .build();
    }
}
// REST API