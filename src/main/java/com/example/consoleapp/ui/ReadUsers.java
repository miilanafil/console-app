package com.example.consoleapp.ui;

import com.example.consoleapp.service.UserService;

import java.util.Objects;


public class ReadUsers implements MenuAction {

    private final UserService userService;
    private final UserTablePrinter printer;

    public ReadUsers(UserService userService, UserTablePrinter printer) {
        this.userService = Objects.requireNonNull(userService, "userService");
        this.printer = Objects.requireNonNull(printer, "printer");
    }

    @Override
    public boolean execute(ConsoleIO io) {
        io.print(printer.render(userService.getAllUsers()));
        return true;
    }
}
