package com.example.consoleapp.service;

import com.example.consoleapp.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(User user);
    boolean deleteUser(Long id);
}