package com.example.consoleapp.ui;

import com.example.consoleapp.dao.UserDAO;
import com.example.consoleapp.entity.User;
import com.example.consoleapp.validation.UserInputParsers;

import java.util.Objects;

public class CreateUser implements MenuAction {

    private final UserDAO userDao;

    public CreateUser(UserDAO userDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
    }

    @Override
    public boolean execute(ConsoleIO io) {
        io.println("Создание пользователя");

        String name = io.read("Имя: ", UserInputParsers.NAME);
        String email = io.read("Email: ", UserInputParsers.EMAIL);
        Integer age = io.read("Возраст: ", UserInputParsers.AGE);

        // DaoException (например, такой email уже есть) обработает Menu
        User created = userDao.create(new User(name, email, age));

        io.println("Пользователь создан, id = " + created.getId());
        return true;
    }
}
