package com.example.consoleapp.ui;

import com.example.consoleapp.service.UserService;
import com.example.consoleapp.entity.User;
import com.example.consoleapp.validation.UserInputParsers;

import java.util.Objects;

public class CreateUser implements MenuAction {

    private final UserService userService;

    public CreateUser(UserService userService) {
        this.userService = Objects.requireNonNull(userService, "userService");
    }

    @Override
    public boolean execute(ConsoleIO io) {
        io.println("Создание пользователя");

        String name = io.read("Имя: ", UserInputParsers.NAME);
        String email = io.read("Email: ", UserInputParsers.EMAIL);
        Integer age = io.read("Возраст: ", UserInputParsers.AGE);

        // DaoException (например, такой email уже есть) обработает Menu
        User created = userService.createUser(new User(name, email, age));

        io.println("Пользователь создан, id = " + created.getId());
        return true;
    }
}
