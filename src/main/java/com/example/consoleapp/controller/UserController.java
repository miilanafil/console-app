package com.example.consoleapp.controller;

import com.example.consoleapp.dto.UserDto;
import com.example.consoleapp.dto.UserRequestDto;
import com.example.consoleapp.dto.UserResponseDto;
import com.example.consoleapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserDto> oldUsers = userService.getAllUsers();
        List<UserResponseDto> responseUsers = oldUsers.stream()
                .map(this::convertToResponseDto)
                .toList();

        return ResponseEntity.ok(responseUsers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(convertToResponseDto(userDto));
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto requestDto) {

        UserDto savedOldUser = userService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedOldUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserRequestDto requestDto) {
        UserDto updatedOldUser = userService.updateUser(id, requestDto);
        return ResponseEntity.ok(convertToResponseDto(updatedOldUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Вспомогательный метод маппинга наружу (создание через new конструктор)
    private UserResponseDto convertToResponseDto(UserDto dto) {
        return new UserResponseDto(
                dto.id(),
                dto.name(),
                dto.email(),
                dto.age(),
                dto.createdCode()
        );
}
}
// REST API