package com.example.consoleapp.ui;

import com.example.consoleapp.dao.UserDAO;
import com.example.consoleapp.validation.UserInputParsers;

import java.util.Objects;

public class DeleteUser implements MenuAction {

    private final UserDAO userDao;

    public DeleteUser(UserDAO userDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
    }

    @Override
    public boolean execute(ConsoleIO io) {
        io.println("Удаление пользователя");

        Long id = io.read("Id: ", UserInputParsers.ID);
        if (!io.confirm("Удалить пользователя с id = " + id + "?")) {
            io.println("Удаление отменено");
            return true;
        }

        boolean deleted = userDao.delete(id);
        io.println(deleted
                ? "Пользователь с id = " + id + " удалён"
                : "Пользователь с id = " + id + " не найден");
        return true;
    }
}
