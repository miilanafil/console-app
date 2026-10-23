package com.example.consoleapp.service;

import com.example.consoleapp.dao.UserDAO;
import com.example.consoleapp.dao.UserDAOImpl;
import com.example.consoleapp.entity.User;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User createUser(User user) {
        validateUser(user);
        user.setName(user.getName().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        return userDAO.create(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        validateId(id);
        return userDAO.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public User updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Пользователь и его ID не могут быть пустыми");
        }
        validateId(user.getId());
        validateUser(user);
        user.setName(user.getName().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        return userDAO.update(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        validateId(id);
        return userDAO.delete(id);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (user.getName().length() > 255) {
            throw new IllegalArgumentException("Имя не должно превышать 255 символов");
        }

        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Некорректный формат email");
        }
        if (user.getEmail().length() > 255) {
            throw new IllegalArgumentException("Email не должен превышать 255 символов");
        }

        if (user.getAge() == null || user.getAge() <= 0) {
            throw new IllegalArgumentException("Возраст должен быть больше нуля");
        }
        if (user.getAge() > 150) {
            throw new IllegalArgumentException("Возраст не может превышать 150 лет");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID должен быть больше нуля");
        }
    }
}