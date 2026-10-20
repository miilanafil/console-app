package com.example.consoleapp.ui;

import com.example.consoleapp.dao.UserDAO;
import com.example.consoleapp.entity.User;
import com.example.consoleapp.validation.InputParser;
import com.example.consoleapp.validation.UserInputParsers;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class UpdateUser implements MenuAction {

    private final UserDAO userDao;

    public UpdateUser(UserDAO userDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
    }

    @Override
    public boolean execute(ConsoleIO io) {
        io.println("Изменение пользователя");

        Long id = io.read("Id: ", UserInputParsers.ID);
        Optional<User> found = userDao.findById(id);
        if (found.isEmpty()) {
            io.println("Пользователь с id = " + id + " не найден");
            return true;
        }


        User user = found.get();
        io.println("Введите новые значения или нажмите Enter, чтобы оставить без изменений.");

        boolean changed = false;
        changed |= askAndSet(io, "Имя", user.getName(), UserInputParsers.NAME, user::setName);
        changed |= askAndSet(io, "Email", user.getEmail(), UserInputParsers.EMAIL, user::setEmail);
        changed |= askAndSet(io, "Возраст", user.getAge(), UserInputParsers.AGE, user::setAge);

        if (!changed) {
            io.println("Изменений нет, пользователь не обновлён");
            return true;
        }

        userDao.update(user);
        io.println("Пользователь с id = " + id + " обновлён");
        return true;
    }

    private static <T> boolean askAndSet(ConsoleIO io, String label, T current,
                                         InputParser<T> parser, Consumer<T> setter) {
        Optional<T> newValue = io.readOptional(label + " [" + current + "]: ", parser);
        if (newValue.isEmpty() || newValue.get().equals(current)) {
            return false;
        }
        setter.accept(newValue.get());
        return true;
    }
}
