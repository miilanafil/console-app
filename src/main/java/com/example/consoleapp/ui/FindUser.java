package com.example.consoleapp.ui;

import com.example.consoleapp.service.UserService;
import com.example.consoleapp.validation.UserInputParsers;

import java.util.Objects;


public class FindUser implements MenuAction {

    private final UserService userService;
    private final UserTablePrinter printer;

    public FindUser(UserService userService, UserTablePrinter printer) {
        this.userService = Objects.requireNonNull(userService, "userService");
        this.printer = Objects.requireNonNull(printer, "printer");
    }

    @Override
    public boolean execute(ConsoleIO io) {
        Long id = io.read("Id: ", UserInputParsers.ID);
        userService.getUserById(id).ifPresentOrElse(
                user -> io.print(printer.renderOne(user)),
                () -> io.println("Пользователь с id = " + id + " не найден"));
        return true;
    }
}
