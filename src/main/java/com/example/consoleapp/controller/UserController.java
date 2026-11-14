package com.example.consoleapp.controller;

import com.example.consoleapp.assembler.UserAssembler;
import com.example.consoleapp.dto.ErrorResponse;
import com.example.consoleapp.dto.UserDto;
import com.example.consoleapp.dto.UserRequestDto;
import com.example.consoleapp.dto.UserResponseDto;
import com.example.consoleapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Пользователи", description = "Создание, просмотр, изменение и удаление пользователей")
public class UserController {

    private final UserService userService;
    private final UserAssembler assembler;

    public UserController(UserAssembler assembler, UserService userService) {
        this.assembler = assembler;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Получить список пользователей",
            description = "Возвращает всех пользователей. У каждого пользователя и у самого списка есть ссылки для навигации.")
    @ApiResponse(responseCode = "200", description = "Список пользователей")
    public CollectionModel<EntityModel<UserDto>> getAllUsers() {
        List<EntityModel<UserDto>> oldUsers = userService.getAllUsers().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(oldUsers, linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по id",
            description = "Возвращает одного пользователя со ссылками на него самого и на список всех пользователей.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "400", description = "Пользователь с таким id не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public EntityModel<UserDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.getUserById(id);
        return assembler.toModel(userDto);
    }

    @PostMapping
    @Operation(summary = "Создать пользователя",
            description = "Создаёт пользователя. После создания на его email уходит письмо.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto requestDto) {
        UserDto savedOldUser = userService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedOldUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить пользователя",
            description = "Заменяет данные пользователя новыми значениями.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь изменён"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDto requestDto) {
        UserDto updatedOldUser = userService.updateUser(id, requestDto);
        return ResponseEntity.ok(convertToResponseDto(updatedOldUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя. После удаления на его email уходит письмо.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь удалён"),
            @ApiResponse(responseCode = "400", description = "Пользователь с таким id не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

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