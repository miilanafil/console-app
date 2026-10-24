package com.example.consoleapp.service;

import com.example.consoleapp.dao.UserDAO;
import com.example.consoleapp.entity.User;
import com.example.consoleapp.exception.DaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Милана", "milka@test.ru", 21);
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Создание: успешное сохранение")
    void createUser_whenValid_shouldCallDaoAndReturnUser() {
        when(userDAO.create(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(testUser);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("milka@test.ru", result.getEmail());
        verify(userDAO, times(1)).create(testUser);
    }

    @Test
    @DisplayName("Создание: пробрасывание ошибки из DAO")
    void createUser_whenDaoThrows_shouldPropagateException() {
        when(userDAO.create(any(User.class))).thenThrow(new DaoException("Ошибка БД"));

        assertThrows(DaoException.class, () -> userService.createUser(testUser));
        verify(userDAO, times(1)).create(testUser);
    }

    @Test
    @DisplayName("Создание: ошибка валидации пустого имени")
    void createUser_whenEmptyName_shouldThrowException() {
        testUser.setName("   ");
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUser));
        verify(userDAO, never()).create(any());
    }

    @Test
    @DisplayName("Создание: ошибка валидации неверного email")
    void createUser_whenInvalidEmail_shouldThrowException() {
        testUser.setEmail("invalid-email");
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUser));
        verify(userDAO, never()).create(any());
    }

    @Test
    @DisplayName("Создание: ошибка валидации возраста")
    void createUser_whenInvalidAge_shouldThrowException() {
        testUser.setAge(-5);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUser));
        verify(userDAO, never()).create(any());
    }

    @Test
    @DisplayName("Поиск по ID: успешный поиск")
    void getUserById_whenFound_shouldReturnUser() {
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
        Optional<User> result = userService.getUserById(1L);
        assertTrue(result.isPresent());
        verify(userDAO, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Поиск по ID: ошибка валидации ID")
    void getUserById_whenInvalidId_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(-1L));
        verify(userDAO, never()).findById(any());
    }

    @Test
    @DisplayName("Получение всех: возврат списка")
    void getAllUsers_shouldReturnList() {
        User secondUser = new User("Карина", "karina@test.ru", 22);
        when(userDAO.findAll()).thenReturn(Arrays.asList(testUser, secondUser));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userDAO, times(1)).findAll();
    }

    @Test
    @DisplayName("Обновление: успешное обновление")
    void updateUser_whenValid_shouldReturnUpdatedUser() {
        testUser.setAge(26);
        when(userDAO.update(testUser)).thenReturn(testUser);

        User result = userService.updateUser(testUser);

        assertEquals(26, result.getAge());
        verify(userDAO, times(1)).update(testUser);
    }

    @Test
    @DisplayName("Обновление: ошибка при отсутствии ID")
    void updateUser_whenNoId_shouldThrowException() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(testUser));
        verify(userDAO, never()).update(any());
    }

    @Test
    @DisplayName("Удаление: успешное удаление")
    void deleteUser_whenValidId_shouldReturnTrue() {
        when(userDAO.delete(1L)).thenReturn(true);
        boolean result = userService.deleteUser(1L);
        assertTrue(result);
        verify(userDAO, times(1)).delete(1L);
    }
}