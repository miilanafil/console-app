package com.example.consoleapp.service;

import com.example.consoleapp.dto.UserDto;
import com.example.consoleapp.dto.UserRequestDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserRequestDto requestDto);
    UserDto getUserById(Long id);
    List<UserDto> getAllUsers();
    UserDto updateUser(Long id, UserRequestDto requestDto);
    void deleteUser(Long id);
}