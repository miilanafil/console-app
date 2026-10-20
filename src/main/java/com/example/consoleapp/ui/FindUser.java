package com.example.consoleapp.ui;

import com.example.consoleapp.dao.UserDAO;
import com.example.consoleapp.validation.UserInputParsers;

import java.util.Objects;


public class FindUser implements MenuAction {

    private final UserDAO userDao;
    private final UserTablePrinter printer;

    public FindUser(UserDAO userDao, UserTablePrinter printer) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
        this.printer = Objects.requireNonNull(printer, "printer");
    }

    @Override
    public boolean execute(ConsoleIO io) {
        Long id = io.read("Id: ", UserInputParsers.ID);
        userDao.findById(id).ifPresentOrElse(
                user -> io.print(printer.renderOne(user)),
                () -> io.println("Пользователь с id = " + id + " не найден"));
        return true;
    }
}
